//package com.openagv.handlers;
//
//import cn.hutool.core.util.ReflectUtil;
//import com.openagv.core.AppContext;
//import com.openagv.core.interfaces.IHandler;
//
//import java.util.Iterator;
//
//public class HandleChain {
//
//    private HandleChain chain;
//
//    public HandleChain() {
//        buildChain();
//    }
//
//    private void buildChain() {
//        for(Iterator<IHandler> iterator = AppContext.getBeforeHeandlerList().iterator(); iterator.hasNext();) {
//
//            IHandler handler =iterator.next();
//            ReflectUtil.newInstance(handler.getClass(), handler);
//
//        }
//    }
//
//    public void next(Request request){
//        if(null != chain) {
//            chain.next(request);
//        }
//    }
//}
