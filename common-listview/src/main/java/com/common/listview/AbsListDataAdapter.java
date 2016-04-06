package com.common.listview;

import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by houlijiang on 2014/9/26.
 * 
 * 列表数据的通用adapter，使用AbsListView时需要继承这个adapter
 * 子类基本只需要重载setData getItemViewHolder两个方法即可
 *
 * 数据的操作使用handler做成串行的
 *
 * 使用注意事项：
 * > 任何对数据的操作都调用notifyDataChanged了，使用时不需要再次调用
 */
public abstract class AbsListDataAdapter<T> extends RecyclerView.Adapter<AbsListDataAdapter.ViewHolder> implements
    IAbsListDataAdapter {

    private static final String TAG = AbsListDataAdapter.class.getSimpleName();

    protected static final int TYPE_LOAD_MORE = Integer.MAX_VALUE;

    public static final int HANDLE_ADD_TO_FRONT = 1;
    public static final int HANDLE_ADD_ALL = 2;
    public static final int HANDLE_ADD_ONE = 3;
    public static final int HANDLE_INSERT = 4;
    public static final int HANDLE_REPLACE = 5;
    public static final int HANDLE_REMOVE = 6;
    public static final int HANDLE_CLEAR_DATA = 7;
    public static final int HANDLE_CLEAR = 8;
    public static final int HANDLE_NO_DATA_CHANGED = 9;
    public static final int HANDLE_RELOADING = 10;
    public static final int HANDLE_EXCHANGE = 11;
    public static final int HANDLE_LOAD_MORE = 12;

    protected List<T> mData;
    protected boolean mIsReloading = false;
    protected boolean mHasMore = true;
    protected int mLoadMoreId = 0;// 加载更多view ID
    private IOnLoadMore mIOnLoadMore;
    // 如果数据变换太频繁会抛Cannot call this method while RecyclerView is computing a layout or scrolling异常
    // 所以这里使用handler把数据修改串行起来
    private Handler mHandler;

    public AbsListDataAdapter() {
        this(null);
    }

    public AbsListDataAdapter(IOnLoadMore loadMore) {
        mData = new ArrayList<>();
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HANDLE_ADD_TO_FRONT: {
                        synchronized (this) {
                            T[] data = (T[]) msg.obj;
                            if (mData == null) {
                                mData = new ArrayList<>();
                                mIsReloading = false;
                            }
                            if (data != null && data.length > 0) {
                                int startIndex = 0;
                                mData.addAll(startIndex, Arrays.asList(data));
                                mIsReloading = false;
                                notifyItemRangeInserted(startIndex, data.length);
                            } else {
                                mIsReloading = false;
                            }
                        }
                        break;
                    }
                    case HANDLE_ADD_ALL: {
                        synchronized (this) {
                            T[] data = (T[]) msg.obj;
                            if (mData == null) {
                                mIsReloading = false;
                                mData = new ArrayList<>();
                            }
                            if (data != null && data.length > 0) {
                                Collections.addAll(mData, data);
                                mIsReloading = false;
                                notifyDataSetChanged();
                            } else {
                                mIsReloading = false;
                            }
                        }
                        break;
                    }
                    case HANDLE_ADD_ONE: {
                        synchronized (this) {
                            mData.add((T) msg.obj);
                            mIsReloading = false;
                            notifyItemInserted(mData.size() - 1);
                        }
                        break;
                    }
                    case HANDLE_INSERT: {
                        synchronized (this) {
                            int position = msg.arg1;
                            mData.add(position, (T) msg.obj);
                            mIsReloading = false;
                            notifyItemInserted(position);
                        }
                        break;
                    }
                    case HANDLE_REPLACE: {
                        synchronized (this) {
                            int position = msg.arg1;
                            if (mData == null || position < 0 || position > mData.size()) {
                                mIsReloading = false;
                                return;
                            }
                            mData.set(position, (T) msg.obj);
                            mIsReloading = false;
                            notifyDataSetChanged();
                        }
                        break;
                    }
                    case HANDLE_REMOVE: {
                        synchronized (this) {
                            int position = msg.arg1;
                            mData.remove(position);
                            mIsReloading = false;
                            notifyItemRemoved(position);
                        }
                        break;
                    }
                    case HANDLE_CLEAR_DATA: {
                        synchronized (this) {
                            mIsReloading = true;
                            if (mData == null) {
                                return;
                            }
                            mData.clear();
                        }
                        break;
                    }
                    case HANDLE_CLEAR: {
                        synchronized (this) {
                            if (mData == null) {
                                mIsReloading = false;
                                return;
                            }
                            mData.clear();
                            mIsReloading = false;
                            notifyDataSetChanged();
                        }
                        break;
                    }
                    case HANDLE_NO_DATA_CHANGED: {
                        synchronized (this) {
                            mIsReloading = false;
                            // 通过假的数据变化通知，来更新列表显示，隐藏加载中等进度
                            notifyDataSetChanged();
                        }
                        break;
                    }
                    case HANDLE_RELOADING: {
                        synchronized (this) {
                            mIsReloading = true;
                            // 通过假的数据变化通知，来更新列表显示，隐藏加载中等进度
                            notifyDataSetChanged();
                        }
                        break;
                    }
                    case HANDLE_EXCHANGE: {
                        synchronized (this) {
                            int i = msg.arg1;
                            int j = msg.arg2;
                            int len = mData == null ? 0 : mData.size();
                            if (i < 0 || i >= len || j < 0 || j >= len || i == j) {
                                mIsReloading = false;
                                break;
                            }
                            T obj = mData.get(i);
                            mData.set(i, mData.get(j));
                            mData.set(j, obj);
                            mIsReloading = false;
                            notifyDataSetChanged();
                        }
                        break;
                    }
                    case HANDLE_LOAD_MORE: {
                        if (mHasMore && mIOnLoadMore != null) {
                            mIOnLoadMore.onLoadMore();
                        }
                        break;
                    }
                }
            }
        };
        mIOnLoadMore = loadMore;
    }

    public void addToFront(T[] data) {
        mHandler.obtainMessage(HANDLE_ADD_TO_FRONT, data).sendToTarget();
    }

    public void addAll(T[] data) {
        if (data == null || data.length == 0) {
            noDataChanged();
        } else {
            mHandler.obtainMessage(HANDLE_ADD_ALL, data).sendToTarget();
        }
    }

    public void add(T obj) {
        mHandler.obtainMessage(HANDLE_ADD_ONE, obj).sendToTarget();
    }

    public void insert(T obj, int position) {
        mHandler.obtainMessage(HANDLE_INSERT, position, 0, obj).sendToTarget();
    }

    public void replace(T obj, int position) {
        mHandler.obtainMessage(HANDLE_REPLACE, position, 0, obj).sendToTarget();
    }

    public void remove(int position) {
        mHandler.obtainMessage(HANDLE_REMOVE, position, 0).sendToTarget();
    }

    public void exchange(int i, int j) {
        mHandler.obtainMessage(HANDLE_EXCHANGE, i, j).sendToTarget();
    }

    public void noDataChanged() {
        mHandler.obtainMessage(HANDLE_NO_DATA_CHANGED).sendToTarget();
    }

    /**
     * 清空数据
     */
    public void clearData() {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_CLEAR_DATA).sendToTarget();
    }

    /**
     * 清空数据并通知数据变化
     */
    public void clear() {
        mHandler.obtainMessage(HANDLE_CLEAR).sendToTarget();
    }

    /**
     * 主动表示在加载中，AbsListView中会判断来显示加载中view
     */
    public void setIsLoading() {
        mIsReloading = true;
        mHandler.obtainMessage(HANDLE_RELOADING).sendToTarget();
    }

    public void setIfHasMore(boolean hasMore) {
        mHasMore = hasMore;
    }

    public boolean isLoadMore(int position) {
        if (mHasMore) {
            return position == mData.size();
        } else {
            return false;
        }
    }

    public List<T> getAllData() {
        return mData;
    }

    public T getData(int position) {
        // AppLog.v(TAG, "get Data for " + position + " item count:" + getItemCount());
        if (position >= getItemCount()) {
            return null;
        }
        return mData.get(position);
    }

    @Override
    final public int getItemViewType(int position) {
        // AppLog.v(TAG, "get view type for " + position);
        if (position == mData.size()) {
            return TYPE_LOAD_MORE;
        }
        return getViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // AppLog.v(TAG, "create view holder for type " + viewType);
        if (viewType == TYPE_LOAD_MORE) {
            View v;
            if (mLoadMoreId != 0) {
                v = LayoutInflater.from(viewGroup.getContext()).inflate(mLoadMoreId, viewGroup, false);
            } else {
                v = new TextView(viewGroup.getContext());
            }
            return new LoadMoreViewHolder(v);
        }
        return getItemViewHolder(viewGroup, viewType);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        // AppLog.v(TAG, "bind view holder for " + position);
        if (viewHolder instanceof LoadMoreViewHolder) {
            // AppLog.v(TAG, "bind view holder for load more");
            mHandler.obtainMessage(HANDLE_LOAD_MORE).sendToTarget();
            return;
        }
        setData(viewHolder, position, getData(position));
    }

    @Override
    public long getItemId(int position) {
        // AppLog.v(TAG, "get view id for " + position);
        return position;
    }

    @Override
    final public int getItemCount() {
        // AppLog.v(TAG, "get count, hasMore " + mHasMore + " size:" + (mData == null ? 0 : mData.size()));
        return (mData == null || mData.size() == 0) ? 0 : (mHasMore ? mData.size() + 1 : mData.size());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private static class LoadMoreViewHolder extends ViewHolder {

        public LoadMoreViewHolder(View itemView) {
            super(itemView);
        }

    }

    /**
     * 判断是否为空，子类可以重载加入自己的逻辑
     * AbsListView在显示时会用来判断以显示empty view
     */
    @Override
    public boolean isEmpty() {
        return mData == null || mData.size() == 0;
    }

    /**
     * AbsListView在显示时会用来判断以显示progress view
     */
    @Override
    public boolean isReloading() {
        return mIsReloading;
    }

    /**
     * 设置加载更多
     */
    @Override
    public void setLoadMoreView(int resourceId) {
        mLoadMoreId = resourceId;
        if (resourceId <= 0) {
            mHasMore = false;
        }
    }

    /**
     * 子类可以重载这个方法，来实现多类型item
     * 
     * @param position index
     * @return 类型
     */
    public int getViewType(int position) {
        return super.getItemViewType(position);
    }

    /**
     * 向每一个item设置数据
     *
     * @param viewHolder 视图
     * @param position index
     */
    protected abstract void setData(ViewHolder viewHolder, int position, T data);

    /**
     * 获取每一条的视图
     *
     * @param type holder类型
     * @return 每一条的视图holder
     */
    protected abstract ViewHolder getItemViewHolder(ViewGroup viewGroup, int type);

    /**
     * 加载跟多回调
     */
    public interface IOnLoadMore {
        void onLoadMore();
    }
}
