package com.robot.mvc.helpers;

import cn.hutool.core.exceptions.UtilException;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.duangframework.db.annotation.DbClient;
import com.duangframework.db.mongodb.MongoDao;
import com.google.inject.Inject;
import com.robot.mvc.core.annnotations.Controller;
import com.robot.mvc.core.annnotations.Import;
import com.robot.mvc.core.annnotations.Service;
import com.robot.mvc.core.exceptions.RobotException;
import com.robot.mvc.core.interfaces.ICacheService;
import com.robot.mvc.model.Route;
import com.robot.utils.GenericsUtils;
import com.robot.utils.ToolsKit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
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

    private static final Logger LOG = LoggerFactory.getLogger(IocHelper.class);

    private static IocHelper IOC_HELPER;
    private final static Lock lock = new ReentrantLock();
    private final static String INJECT_SERVICE_METHOD_NAME = "InjectService";
    private final static String CURD_SERVICE_NAME = "CurdService";
    private final static String CURD_CACHE_SERVICE_NAME = "CurdCacheService";


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

    private Object injectServiceMethod(Object serviceObj, Class serviceClazz) {
        if (!CURD_SERVICE_NAME.equalsIgnoreCase(serviceClazz.getSuperclass().getSimpleName())) {
            return serviceObj;
        }

        Method[] methods = serviceClazz.getMethods();
        if (ToolsKit.isEmpty(methods)) {
            return serviceObj;
        }
        boolean isInjectServiceMethod = false;
        for (Method method : methods) {
            if (INJECT_SERVICE_METHOD_NAME.equals(method.getName())) {
                isInjectServiceMethod = true;
                break;
            }
        }

        if (!isInjectServiceMethod) {
            LOG.warn("[{}]里没有实现[{}]方法，请检查！", CURD_SERVICE_NAME, INJECT_SERVICE_METHOD_NAME);
            return serviceObj;
        }
//        Constructor constructor = ReflectUtil.getConstructor(serviceClazz, MongoDao.class, ICacheService.class);
//        if (null == constructor) {
//            return serviceObj;
//        }
        // 泛型<>里的类
        Class<?> generiscClass = GenericsUtils.getSuperClassGenricType(serviceClazz);
        Field mongoDaoField = null;
        Field[] fields = serviceClazz.getDeclaredFields();
        if (null != fields) {
            for (Field field : fields) {
                if (MongoDao.class.equals(field.getType())) {
                    ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                    Type[] types = paramType.getActualTypeArguments();
                    if (ToolsKit.isNotEmpty(types)) {
                        // <>里的泛型类
                        if (types[0].getClass().equals(generiscClass)) {
                            mongoDaoField = field;
                            break;
                        }
                    }
                }
            }
        }
        MongoDao dbDao = (MongoDao) getDbInjectDao(mongoDaoField, generiscClass);
        if (null == dbDao) {
            throw new NullPointerException("对[" + serviceClazz.getName() + "]进行构造方法注入时，MongoDao为null");
        }
        ICacheService cacheService = null;
        for (Iterator<Map.Entry<String, Route>> iterator = RouteHelper.duang().getServiceRouteMap().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Route> entry = iterator.next();
            Route route = entry.getValue();
            Class cacheServiceClass = route.getServiceClass();
            if (CURD_CACHE_SERVICE_NAME.equalsIgnoreCase(cacheServiceClass.getSuperclass().getSimpleName())) {
                Class cacheGenericsClass = GenericsUtils.getSuperClassGenricType(cacheServiceClass);
                if (generiscClass.equals(cacheGenericsClass)) {
                    cacheService = (ICacheService) route.getServiceObj();
                    break;
                }
            }
        }
        if (null == cacheService) {
            throw new NullPointerException("对[" + serviceClazz.getName() + "]进行构造方法注入时，cacheService为null，" +
                    "请检查是否已经按规则创建了[" + generiscClass.getSimpleName() + "CacheService]!");
        }
        try {
            ReflectUtil.invoke(serviceObj, INJECT_SERVICE_METHOD_NAME, dbDao, cacheService);
//            serviceObj = ReflectUtil.newInstance(serviceClazz, dbDao, cacheService);
        } catch (UtilException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
//        Route route = RouteHelper.duang().getServiceRoute(serviceClazz);
//        route.setServiceObj(serviceObj);
//        RouteHelper.duang().getServiceRouteMap().put(serviceClazz.getName(), route);
//        BeanHelper.duang().setBean(serviceObj);
        return serviceObj;
    }

    public void ioc() throws Exception {
        Iterator<Map.Entry<String, Route>> iterator =
                Optional.ofNullable(RouteHelper.duang().getRoutes().entrySet().iterator()).orElseThrow(NullPointerException::new);
        while (iterator.hasNext()) {
            Map.Entry<String, Route> entry = iterator.next();
            Route route = Optional.ofNullable(entry.getValue()).orElseThrow(NullPointerException::new);
            Object serviceObj = injectServiceMethod(route.getServiceObj(), route.getServiceClass());
            ioc(serviceObj, serviceObj.getClass());
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
            String dbClientId = "";
            if (null != field) {
                DbClient dbClient = field.getAnnotation(DbClient.class);
                dbClientId = ToolsKit.isNotEmpty(dbClient) ? dbClient.id() : "";
                if (ToolsKit.isEmpty(dbClientId)) {
                    Import importAnn = field.getAnnotation(Import.class);
                    dbClientId = importAnn.client();
                }
            }
//            List<?> proxyList = null;
//            dbDaoObj =  MongoUtils.getMongoDao(dbClientId, paramTypeClass, proxyList);
            dbDaoObj = ReflectUtil.newInstance(MongoDao.class, dbClientId, paramTypeClass);
            DB_DAO_MAP.put(key, dbDaoObj);
        }
        return DB_DAO_MAP.get(key);
    }

}
