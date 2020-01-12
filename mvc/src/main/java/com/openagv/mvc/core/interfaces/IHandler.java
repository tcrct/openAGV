package com.openagv.mvc.core.interfaces;

import com.openagv.mvc.core.exceptions.AgvException;

/**
 * 处理器接口
 * Created by laotang on 2020/1/12.
 */
public interface IHandler {

    /**
     * 处理方法
     *
     * @param target    协议指令
     * @param request 请求对象
     * @param response 返回对象
     * @Exception AgvException
     * @return 处理成功返回true
     */
    boolean doHandler(String target, IRequest request, IResponse response) throws AgvException;

}
