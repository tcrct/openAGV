package com.robot.event;

import com.robot.event.core.EventFactory;
import com.robot.event.core.EventListener;
import com.robot.event.core.EventModel;
import com.robot.utils.ToolsKit;

/**
 * 事件执行工具类
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-30
 */
public class EventKit {

    private static class EventKitHolder {
        private static final EventKit INSTANCE = new EventKit();
    }
    private EventKit() {
    }
    public static final EventKit duang() {
        clear();
        return EventKitHolder.INSTANCE;
    }
    /*****************************************************************************/

    private static Object _param;
    private static String _key;
    private static boolean _isAsync;

    private static void clear() {
        _param = null;
        _key = "";
        _isAsync = false;
    }

    /**
     * 发送的参数
     * @param param
     * @return
     */
    public EventKit param(Object param) {
        _param = param;
        return this;
    }

    /**
     * 是否同步处理，默认为同步，当值为true时为异步
     * @param async
     * @return
     */
    public EventKit isAsync(boolean async) {
        _isAsync = async;
        return this;
    }

    /**
     *  Listener注解类
     * @param  listenerClass Listener注解类
     * @return
     */
    public EventKit listener(Class<? extends EventListener> listenerClass) {
        listenerKey(listenerClass.getName());
        return this;
    }

    /**
     *  Listener注解设置的key值
     * @param key 关键字值
     * @return
     */
    public EventKit listenerKey(String key) {
        _key = key;
        return this;
    }

    /**
     * 执行请求
     * @return
     */
    public <T> T execute() {
        if(ToolsKit.isEmpty(_param)) {
            throw new NullPointerException("value is null");
        }
        return EventFactory.getInstance().executeEvent(new EventModel.Builder().key(_key).value(_param).isSync(_isAsync).build());
    }

}
