package com.robot.mvc.helpers;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.duangframework.db.annotation.DbClient;
import com.duangframework.db.mongodb.MongoDao;
import com.google.inject.Inject;
import com.robot.mvc.core.annnotations.Controller;
import com.robot.mvc.core.annnotations.Import;
import com.robot.mvc.core.annnotations.Service;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.model.Route;
import com.robot.utils.ToolsKit;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * 依赖注入
 * Created by laotang on 2019/11/3.
 */
public class IocHelper {

    private static IocHelper IOC_HELPER;
    private final static Lock lock = new ReentrantLock();

    public static IocHelper duang() {
        try {
            lock.lock();
            if (null == IOC_HELPER) {
                IOC_HELPER = new IocHelper();
            }
        } finally {
            lock.unlock();
        }
        return IOC_HELPER;
    }

    private IocHelper() {
        try {
            ioc();
        } catch (Exception e) {
            throw new RobotException(e.getMessage(), e);
        }
    }

    public void ioc() throws Exception {
        Iterator<Map.Entry<String, Route>> iterator = Optional.ofNullable(RouteHelper.duang().getRoutes().entrySet().iterator()).orElseThrow(NullPointerException::new);
        while (iterator.hasNext()) {
            Map.Entry<String, Route> entry = iterator.next();
            Route route = Optional.ofNullable(entry.getValue()).orElseThrow(NullPointerException::new);
            Object serviceObj = route.getServiceObj();
            Class<?> clazz = serviceObj.getClass();
            ioc(serviceObj, clazz);
        }
    }

    public void ioc(Object serviceObj, Class clazz) throws Exception {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            boolean isInjectAnn = field.isAnnotationPresent(Import.class) ||
                    field.isAnnotationPresent(Inject.class) ||
                    field.isAnnotationPresent(javax.inject.Inject.class);
            if (isInjectAnn) {
                Class<?> fieldTypeClass = field.getType();
                if (fieldTypeClass.equals(clazz)) {
                    throw new RobotException(clazz.getSimpleName() + " can't not already import " + fieldTypeClass.getSimpleName());
                }
                if (!MongoDao.class.equals(field.getType()) &&
                        (fieldTypeClass.isAnnotationPresent(Controller.class) || fieldTypeClass.isAnnotationPresent(Service.class)) ) {
                    Object iocServiceObj = Optional.ofNullable(BeanHelper.duang().getBean(fieldTypeClass)).orElseThrow(NullPointerException::new);
                    field.setAccessible(true);
                    field.set(serviceObj, iocServiceObj);
                } else if (MongoDao.class.equals(field.getType())) {
                    ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                    Type[] types = paramType.getActualTypeArguments();
                    if (ToolsKit.isNotEmpty(types)) {
                        // <>里的泛型类
                        Class<?> paramTypeClass = ClassUtil.loadClass(types[0].getTypeName());
                        Object daoObj = getDbInjectDao(field, paramTypeClass);
                        if (null != daoObj) {
                            field.setAccessible(true);
                            field.set(serviceObj, daoObj);
                        }
                    }
                }
            }
        }
    }

    private static final Map<String, Object> DB_DAO_MAP = new HashMap<>();

    private static Object getDbInjectDao(Field field, Class<?> paramTypeClass) {
        String key = paramTypeClass.getName();
        Object dbDaoObj = DB_DAO_MAP.get(key);
        if (ToolsKit.isEmpty(dbDaoObj)) {
            DbClient dbClient = field.getAnnotation(DbClient.class);
            String dbClientId = ToolsKit.isNotEmpty(dbClient) ? dbClient.id() : "";
            if (ToolsKit.isEmpty(dbClientId)) {
                Import importAnn = field.getAnnotation(Import.class);
                dbClientId = importAnn.client();
            }
//            List<?> proxyList = null;
//            dbDaoObj =  MongoUtils.getMongoDao(dbClientId, paramTypeClass, proxyList);
            dbDaoObj = ReflectUtil.newInstance(MongoDao.class, dbClientId, paramTypeClass);
            DB_DAO_MAP.put(key, dbDaoObj);
        }
        return DB_DAO_MAP.get(key);
    }

}
