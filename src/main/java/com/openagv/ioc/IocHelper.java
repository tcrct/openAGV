package com.openagv.ioc;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.inject.Inject;
import com.openagv.core.AppContext;
import com.openagv.db.annotation.DbClient;
import com.openagv.db.mongodb.MongoDao;
import com.openagv.tools.ToolsKit;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by laotang on 2019/11/3.
 */
public class IocHelper {

    public static void ioc() throws Exception {

        Iterator<Object> iterator = AppContext.getInjectClassObjectSet().iterator();
        while (iterator.hasNext()) {
            Object serviceObj = iterator.next();
            Class<?> clazz = serviceObj.getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                boolean isInjectAnn = field.isAnnotationPresent(Inject.class) || field.isAnnotationPresent(javax.inject.Inject.class);
                if (isInjectAnn && MongoDao.class.equals(field.getType())) {
                    ParameterizedType paramType = (ParameterizedType) field.getGenericType();
                    Type[] types = paramType.getActualTypeArguments();
                    if (ToolsKit.isNotEmpty(types)) {
                        // <>里的泛型类
                        Class<?> paramTypeClass = ClassUtil.loadClass(types[0].getTypeName());
                        Object daoObj = getDbInjectDao(field, paramTypeClass); //MongoUtils.getMongoDao(key, paramTypeClass, proxyList);
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
        if(ToolsKit.isEmpty(dbDaoObj)) {
            // TODO 如果需要多数据源，可以在这里动手脚
//            DbClient dbClient = field.getAnnotation(DbClient.class);
//            String dbClientId = dbClient.id();
            dbDaoObj = ReflectUtil.newInstance(MongoDao.class, paramTypeClass);
            DB_DAO_MAP.put(key, dbDaoObj);
        }
        return DB_DAO_MAP.get(key);
    }

}
