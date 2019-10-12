package com.openagv.core.interfaces;


/**
 * 模板接口
 * 定义算法的结构
 *
 * @author Laotang
 */
public interface IAction {

    /**
     * 动作名称
     * @return
     */
    String actionKey();

    /**
     *执行操作
     */
    void execute() throws Exception;

}
