package com.openagv.db;

public interface IDao<T> {
    /**
     * 保存对象
     * @param  entity		待保存的对象
     * @return		成功返回true
     */
    <T> T save(T entity) throws Exception;

}
