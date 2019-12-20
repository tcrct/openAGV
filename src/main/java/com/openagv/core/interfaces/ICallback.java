package com.openagv.core.interfaces;

public interface ICallback<T> {

    void call(String deviceId, T object, String vechileName) throws Exception;

}
