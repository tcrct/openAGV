package com.openagv.db.mongodb;


import cn.hutool.core.util.ReflectUtil;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.openagv.db.annotation.ClientId;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class MongoDao<T> extends MongoBaseDao<T>  {

    public MongoDao(){

    }

    private void init() {
//        cls = ((Class<T>)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0]);
//        System.out.println("@@@@@@@: " + ((ParameterizedType)getClass().getGenericSuperclass()).getOwnerType());
//        System.out.println("@@@@@@@: " + ((ParameterizedType)getClass().getGenericSuperclass()).getRawType());
//        Type type = ((ParameterizedType)MongoDao.class.getGenericSuperclass()).getActualTypeArguments()[0];
//        System.out.println("@@@@@@@: " + type.getClass().getSimpleName());
    }

    public MongoDao(Class<T> clazz) {
        super(clazz);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@##########################: " + clazz);
    }

//    public MongoDao(DB db, MongoDatabase database, Class<T> clazz) {
//        super(db, database, clazz);
//    }


}
