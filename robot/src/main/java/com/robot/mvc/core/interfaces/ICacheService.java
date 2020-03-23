package com.robot.mvc.core.interfaces;

/**
 * 缓存层接口
 *
 * @author  Laotang
 * @since 1.0
 * @date 2020/3/22.
 */
public interface ICacheService<T> {

    /**
     * 保存到缓存
     * @param entity
     */
    void save(T entity);

    /**
     * 根据ID查找记录
     * @param id 记录id
     * @return
     */
    T findById(String id);

    /**
     * 根据ID删除记录
     */
    boolean deleteById(String id);
}
