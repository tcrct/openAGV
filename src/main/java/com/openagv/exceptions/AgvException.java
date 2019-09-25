package com.openagv.exceptions;

import cn.hutool.core.util.ObjectUtil;

/**
 * Created by laotang on 2019/9/25.
 */
public class AgvException extends AbstractDuangException{

    public AgvException() {
        super();
    }

    public AgvException(String msg) {
        super(msg);
    }

    public AgvException(int code, String msg) {
        super(code, msg);
    }

    public AgvException(String msg , Throwable cause) {
        super(msg, cause);
    }

    @Override
    public int getCode() {
        return IException.FAIL_CODE;
    }

    @Override
    public String getMessage() {
        if(ObjectUtil.isEmpty(super.getMessage())) {
            return IException.FAIL_MESSAGE;
        } else {
            return super.getMessage();
        }
    }
}
