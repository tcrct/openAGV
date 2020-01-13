package com.makerwit.service;

import com.openagv.mvc.core.annnotations.Service;
import com.openagv.mvc.core.interfaces.IRequest;
import com.openagv.mvc.core.interfaces.IResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by laotang on 2020/1/14.
 */
@Service
public class A001Service {

    private static final Logger LOG = LoggerFactory.getLogger(A001Service.class);

    /**
     * 初始化
     */
    public void init() {

    }

    /***
     * 下发移动指令
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String setRout(IRequest request, IResponse response) throws Exception {
        LOG.info("###############: " + request.getRawContent());
        return "";
    }

}
