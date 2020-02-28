package com.robot.mvc.model;

/**
 * 返回对象
 * @param <T>
 */
public class ReturnDto<T> implements java.io.Serializable {
    /**
     * 返回头部信息Dto
     */
    private HeadDto head;
    /**
     * 返回内容主体对象
     */
    private T data;

    public ReturnDto() {
    }

    public ReturnDto(HeadDto head, T data) {
        this.head = head;
        this.data = data;
    }

    public HeadDto getHead() {
        return head;
    }

    public void setHead(HeadDto head) {
        this.head = head;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
