package com.openagv.mvc.core.telegram;

import cn.hutool.http.HttpStatus;
import com.openagv.mvc.core.interfaces.IResponse;

/**
 * Created by laotang on 2020/1/12.
 */
public class BaseResponse implements IResponse {

    private String id;
    private int status;
    private Exception exception;

    public BaseResponse(String requestId) {
        this.id = requestId;
        setStatus(HttpStatus.HTTP_OK);
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setStatus(int status) {
        this.status =status;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public void write(Object message) {

    }

    /**设置异常信息*/
    public void setException(Exception exception) {
        this.exception = exception;
    }
    @Override
    public Exception getException() {
        return exception;
    }
}
