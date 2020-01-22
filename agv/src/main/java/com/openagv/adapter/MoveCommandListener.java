package com.openagv.adapter;

import cn.hutool.http.HttpStatus;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.telegram.MoveRequest;
import com.openagv.mvc.main.DispatchFactory;
import org.opentcs.drivers.vehicle.MovementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by laotang on 2020/1/22.
 */
public class MoveCommandListener implements ActionListener {

    private static final Logger LOG = LoggerFactory.getLogger(MoveCommandListener.class);
    /***/
    private Queue<MovementCommand> commandQueue;
    /**是否需要发送，true时为需要发送*/
    private boolean isNeetSend;
    /**车辆适配器*/
    private AgvCommAdapter adapter;

    public MoveCommandListener(AgvCommAdapter adapter) {
        this.adapter = adapter;
        this.isNeetSend = false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // 如果不需要发送则直接退出
        if (!isNeetSend) {
            return;
        }
        // 如果命令队列为空，则退出
        if ((null == commandQueue || commandQueue.isEmpty())) {
            return;
        }

        //进行业务处理
        try {
            // 将请求发送到业务逻辑处理，自行实现所有的协议内容发送
            DispatchFactory.dispatch(new MoveRequest(adapter, new ArrayList<>(commandQueue)));
        } catch (Exception ex) {
            throw new AgvException("创建移动协议指令时出错: "+ex.getMessage(), ex);
        }
        // 发送开关，已经发送设置为false，防止重复执行
        isNeetSend = false;
    }

    /**
     * 取得移动命令队列引用
     * @param commandQueue 移动命令队列
     */
    public void quoteCommand(Queue<MovementCommand> commandQueue) {
        this.commandQueue = commandQueue;
        isNeetSend = true;
    }
}
