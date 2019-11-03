package com.openagv.db.mongodb;

import com.openagv.db.IDao;
import com.openagv.db.annotation.DbClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MongoBaseDao<T> implements IDao<T> {

    private final static Logger logger = LoggerFactory.getLogger(MongoBaseDao.class);

    protected Class<?> cls;

    public MongoBaseDao() {
        System.out.println("#######MongoBaseDao########");
//        init();
    }


    private void init() {
//        cls = ((Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
        System.out.println("@@@@@@@: " + ((ParameterizedType)getClass().getGenericSuperclass()).getOwnerType());
        System.out.println("@@@@@@@: " + ((ParameterizedType)getClass().getGenericSuperclass()).getRawType().getClass().getGenericSuperclass());
        Type type = ((ParameterizedType)MongoDao.class.getGenericSuperclass()).getActualTypeArguments()[0];
        T t = (T)type.getClass();
        System.out.println("@@@@@@@: " + t.getClass());
        this.cls = getSuperClassGenricType(getClass(), 0);
        System.out.println("@####$$$$:" + cls);
    }

    public static Class getSuperClassGenricType(final Class clazz, final int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
            logger.warn(clazz.getSimpleName() + "'s superclass not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
            logger.warn("Index: " + index + ", Size of " + clazz.getSimpleName() + "'s Parameterized Type: "
                    + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            logger.warn(clazz.getSimpleName() + " not set the actual class on superclass generic parameter");
            return Object.class;
        }

        return (Class) params[index];
    }

    public MongoBaseDao(Class<T> cls){
        this.cls = cls;
        print();
    }

    @Override
    public <T> T save(T entity) throws Exception {
        System.out.println("entity.getClass().getSimpleName(): " + entity.getClass().getSimpleName());
        print();
        return null;
    }

    public void print() {
        DbClient dbClient = cls.getAnnotation(DbClient.class);
        if(null != dbClient) {
            System.out.println("#################print: " + dbClient.id());
        }
    }

}
