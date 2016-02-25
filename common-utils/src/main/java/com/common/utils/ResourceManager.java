package com.common.utils;

import android.util.Log;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by houlijiang on 15/11/19.
 * 
 * 资源管理，根据传入的object的生命周期自动cancel相关资源
 * 可以用来管理网络请求，当关联的object被回收则关闭对应的网络请求
 */
public class ResourceManager {

    private static final String TAG = ResourceManager.class.getSimpleName();

    // 默认对象，生命周期同RequestManager
    private Object mDefaultObj = new Object();
    // 检查线程
    private RequestCheckThread mCheckThread = new RequestCheckThread();
    // 为了快速检索到object对应的请求队列，使用object.hashCode作为key，这样不会对object有引用
    private Map<Integer, RequestsReference> mRequestRefMap = new ConcurrentHashMap<>();
    // 虚拟引用被回收后的队列
    private ReferenceQueue mReferenceQueue = new ReferenceQueue();
    // 检查线程的控制变量
    private boolean mCheckRunning = true;

    private static class InstanceHolder {
        public final static ResourceManager instance = new ResourceManager();
    }

    public static ResourceManager getInstance() {
        return InstanceHolder.instance;
    }

    private ResourceManager() {
    }

    public void init(){
        mCheckThread.start();
    }

    public void release() {
        mDefaultObj = null;
        mCheckRunning = false;
        mCheckThread.interrupt();
    }

    /**
     * 添加请求监控，请求的生命周期跟随传入的obj的生命周期，如果obj被回收则对应的所有请求都会被cancel
     * 
     * @param obj 对象，用该obj的生命周期控制请求的生命周期
     * @param call 可取消资源
     */
    public void addRequest(Object obj, Cancelable call) {
        // 先判断是否已经存在了
        if (mRequestRefMap.containsKey(obj.hashCode())) {
            RequestsReference r = mRequestRefMap.get(obj.hashCode());
            r.add(call);
        } else {
            RequestsReference ref = new RequestsReference(obj, mReferenceQueue, call);
            mRequestRefMap.put(obj.hashCode(), ref);
        }

    }

    /**
     * 删除可取消资源的监控
     * 
     * @param obj 对象
     * @param call 可取消资源
     */
    public void removeRequest(Object obj, Cancelable call) {
        if (mRequestRefMap.containsKey(obj.hashCode())) {
            RequestsReference r = mRequestRefMap.get(obj.hashCode());
            r.remove(call);
        }
    }

    /**
     * 添加整个manager生命周期的可取消资源监控
     */
    public void addRequest(Cancelable call) {
        addRequest(mDefaultObj, call);
    }

    /**
     * 删除可取消资源的监控
     * 
     * @param call 可取消资源
     */
    public void removeRequest(Cancelable call) {
        removeRequest(mDefaultObj, call);
    }

    /**
     * 自定义虚拟引用，加了一个队列来记录所有同一个object的请求
     */
    private static class RequestsReference extends PhantomReference {

        private int hashCode;
        private List<Cancelable> list;

        public RequestsReference(Object r, ReferenceQueue q, Cancelable closeable) {
            super(r, q);
            hashCode = r.hashCode();
            list = Collections.synchronizedList(new LinkedList<Cancelable>());
            list.add(closeable);
        }

        public int getId() {
            return hashCode;
        }

        /**
         * 添加到监控队列中
         * 
         * @param call 可取消资源
         */
        public void add(Cancelable call) {
            list.add(call);
        }

        /**
         * 从监控队列中移除
         * 
         * @param call 可取消资源
         */
        public void remove(Cancelable call) {
            list.remove(call);
        }

        /**
         * 取消监控的所有可取消资源
         */
        public void cancelAll() {
            for (Cancelable closeable : list) {
                try {
                    closeable.cancel();
                } catch (Exception e) {
                    Log.e(TAG, "cancel exception, e:" + e.getLocalizedMessage());
                }
            }
            list.clear();
            list = null;
        }
    }

    /**
     * 等待回收队列里有了被回收的对象后，把对应的请求都取消掉
     */
    private final class RequestCheckThread extends Thread {

        public RequestCheckThread() {
            super("RequestCheckThread");
            setPriority(Thread.MAX_PRIORITY);
            setDaemon(true);
        }

        /**
         * 等待回收队列里有数据
         */
        public void run() {
            while (mCheckRunning) {
                RequestsReference ref;
                try {
                    ref = (RequestsReference) mReferenceQueue.remove();
                } catch (Exception e) {
                    Log.e(TAG, "request check thread catch e:" + e.getLocalizedMessage());
                    continue;
                }
                if (ref != null) {
                    ref.cancelAll();
                    ref.clear();
                    mRequestRefMap.remove(ref.getId());
                }
            }
        }
    }

    /**
     * 可以取消的类的接口，所有需要管理的资源都要实现这个接口
     */
    public interface Cancelable {
        boolean cancel();
    }

}
