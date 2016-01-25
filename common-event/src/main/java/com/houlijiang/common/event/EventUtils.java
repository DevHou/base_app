package com.houlijiang.common.event;


import com.houlijiang.common.event.eventbus.OpenEventBus;

/**
 * Created by houlijiang on 2014/4/1.
 *
 * 异步事件通知
 */
public class EventUtils {

    private static IEventBus bus;
    static {
        bus = new OpenEventBus();
    }

    /**
     * 注册事件订阅
     */
    public static void registerEvent(Object obj) {
        bus.registerEvent(obj);
    }

    /**
     * 取消注册事件订阅
     */
    public static void unRegisterEvent(Object obj) {
        bus.unRegisterEvent(obj);
    }

    /**
     * 广播事件
     */
    public static void postEvent(Object event) {
        bus.postEvent(event);
    }

    /**
     * 广播sticky事件
     */
    public static void postStickyEvent(Object event) {
        bus.postStickyEvent(event);
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
        bus.removeAllStickyEvent();
    }

    /**
     * 取消事件递送
     */
    public static void cancelEventDelivery(Object event) {
        bus.cancelEventDelivery(event);
    }
}
