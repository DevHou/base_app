package com.houlijiang.common.event;

/**
 * Created by houlijiang on 15/11/20.
 * 
 * event异步处理的接口
 * 
 * 普通事件发布后直接查找注册的处理函数调用执行，然后事件丢弃
 * 
 * sticky事件则会一直保留，并且是先发送事件再在处理者注册处理时回调处理函数的
 * 同时 sticky事件不会被主动丢弃，必须主动调用remove才能删除
 * 
 * sticky事件可以当做普通事件处理，唯一区别就是上面说的特别的处理
 */
public interface IEventBus {

    /**
     * 注册事件订阅
     */
    void registerEvent(Object obj);

    /**
     * 取消注册事件订阅
     */
    void unRegisterEvent(Object obj);

    /**
     * 广播事件
     */
    void postEvent(Object event);

    /**
     * 取消事件递送，在事件处理链中，可以取消传递，后面注册的事件处理则不会再被调用
     */
    void cancelEventDelivery(Object event);

    /**
     * 注册sticky处理，这时直接查找是否有sticky事件，如果有则直接执行
     * 
     * 同时这个obj也可以写和普通event同样的onEvent方法注册处理普通event
     */
    void registerSticky(Object obj);

    /**
     * 广播sticky事件
     */
    void postStickyEvent(Object event);

    /**
     * 删除sticky事件，如果传入对象，则必须是原注册的对象，否则请使用下面那个重载
     */
    void removeStickyEvent(Object event);

    /**
     * 删除sticky事件，根据类类型删除
     */
    <T> T removeStickyEvent(Class<T> eventType);

    /**
     * 删除所有sticky事件
     */
    void removeAllStickyEvent();

}
