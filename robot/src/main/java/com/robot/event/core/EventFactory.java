package com.robot.event.core;

import cn.hutool.core.thread.ThreadUtil;
import com.robot.mvc.helpers.BeanHelper;
import com.robot.utils.ToolsKit;

import java.util.concurrent.ConcurrentHashMap;

/**
 *  事件处理器工厂，用于处理事件，使程序解耦
 *
 * @author Laotang
 * @since 1.0
 * @date 2020-03-30
 */
public class EventFactory {

    private static EventFactory eventFactory;
    private static ConcurrentHashMap<String, EventListener> eventListenerMap = new ConcurrentHashMap<>();

    public static EventFactory getInstance() {
        if(null == eventFactory) {
            eventFactory = new EventFactory();
        }
        return eventFactory;
    }

    private EventFactory() {

    }
    public <T> T executeEvent(EventModel model){
        String key = model.getKey();
        EventListener eventListener = BeanHelper.duang().getBean(key);
        if(ToolsKit.isEmpty(eventListener)){
            throw new NullPointerException("find eventListener["+key+"] is null");
        }
        return exceute(eventListener,new Event(model.getModel()), model.isAsync());
    }

    @SuppressWarnings("unchecked")
    private <T> T exceute(final EventListener eventListener, final Event event, final boolean async) {
//		Type type = ((ParameterizedType) eventListener.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[1];
        if(async){
            ThreadUtil.execute(new Thread(){
                public void run() {
                    eventListener.onEvent(event);
                }
            });
            return (T)null;		//如果是异步的话，就直接返回null;
        } else {
            return (T) eventListener.onEvent(event);
        }
    }

}
