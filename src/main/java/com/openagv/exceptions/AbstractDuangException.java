package com.openagv.exceptions;

/**
 * @author Created by laotang
 * @date createed in 2018/7/5.
 */
public abstract class AbstractDuangException extends RuntimeException implements IException {

    protected int code = IException.FAIL_CODE;
    protected String message = ""; //IException.FAIL_MESSAGE;

    AbstractDuangException() {
        super();
    }

    AbstractDuangException(String msg) {
        super(msg);
        setMessage(msg);
    }

    AbstractDuangException(Integer code, String msg) {
        this(msg);
        setCode(code);
    }

    public AbstractDuangException(String msg , Throwable cause) {
        super(msg, cause);
        setMessage(msg);
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
