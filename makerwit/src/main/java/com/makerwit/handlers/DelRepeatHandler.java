package com.makerwit.handlers;

import com.makerwit.core.component.Protocol;
import com.makerwit.core.component.RepeatSend;
import com.makerwit.numes.MakerwitEnum;
import com.openagv.mvc.core.exceptions.AgvException;
import com.openagv.mvc.core.interfaces.IHandler;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  根据返回的车辆标识符及验证码删除重发队列里的元素
 *
 * @author Laotang
 */
public class DelRepeatHandler implements IHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DelRepeatHandler.class);

    @Override
    public boolean doHandler(String target, IRequest request, IResponse response) throws AgvException {
        Protocol protocol = (Protocol) request.getProtocol();

        String direction = protocol.getDirection();
        // 凡是响应上报的(r)，均需要进行查询重发队列里是否有元素存在，如果存在，则remove
        if (MakerwitEnum.DOWN_LINK.getValue().equals(direction)) {
            try {
                RepeatSend.duang().remove(request);
                // 返回false，让系统不继续向下执行，结束处理
                // 返回false与抛出异常的区别在于，返回false，系统直接当作丢弃处理，不往下执行代码
                // 抛出异常，系统捕捉到异常抛出后，将异常信息返回到客户端
                return false;
            } catch (AgvException e) {
                throw e;
            }
        }
        return true;
    }
}
