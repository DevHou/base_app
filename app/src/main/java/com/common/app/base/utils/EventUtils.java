package com.common.app.base.utils;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by houlijiang on 16/3/15.
 * 
 * event工具类
 */
public class EventUtils {

    private static EventBus bus;
    static {
        bus = EventBus.getDefault();
    }

    /**
     * 注册事件订阅
     */
    public static void register(Object obj) {
        bus.register(obj);
    }

    /**
     * 取消注册事件订阅
     */
    public static void unregister(Object obj) {
        bus.unregister(obj);
    }

    /**
     * 广播事件
     */
    public static void postEvent(Object event) {
        bus.post(event);
    }

    /**
     * 广播sticky事件
     */
    public static void postStickyEvent(Object event) {
        bus.postSticky(event);
    }

    /**
     * 删除sticky事件，如果传入对象，则必须是原注册的对象，否则请使用下面那个重载
     */
    public static void removeStickyEvent(Object event) {
        bus.removeStickyEvent(event);
    }

    /**
     * 删除sticky事件
     */
    public static <T> T removeStickyEvent(Class<T> eventType) {
        return bus.removeStickyEvent(eventType);
    }

    /**
     * 删除所有sticky事件
     */
    public static void removeAllStickyEvent() {
        bus.removeAllStickyEvents();
    }

    /**
     * 取消事件递送
     */
    public static void cancelEventDelivery(Object event) {
        bus.cancelEventDelivery(event);
    }

}
