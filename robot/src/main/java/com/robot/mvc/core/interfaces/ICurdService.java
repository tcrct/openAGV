package com.robot.mvc.core.interfaces;

import com.duangframework.db.common.Page;
import com.robot.utils.NameValuePair;

import java.io.Serializable;
import java.util.List;

/**
 * 指令服务业务逻辑处理接口
 *
 * @author Laotang
 * @date 2020/2/3
 * @since 1.0
 */
public interface ICurdService<T> extends IService {

    T save(T entity);

    T findById(String id);

    List<T> findAll(List<String> fields, NameValuePair... nameValuePairs);

    boolean deleteById(String id);

    Page<T> search(Serializable serializable);
}
