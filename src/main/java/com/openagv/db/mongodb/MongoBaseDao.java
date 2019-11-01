package com.openagv.db.mongodb;

import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.openagv.db.IDao;
import com.openagv.db.annotation.ClientId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoBaseDao<T> implements IDao<T> {

    private final static Logger logger = LoggerFactory.getLogger(MongoBaseDao.class);

    protected Class<?> cls;

    public MongoBaseDao() {
        System.out.println("#######MongoBaseDao########");
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
        ClientId clientId = cls.getAnnotation(ClientId.class);
        if(null != clientId) {
            System.out.println("#################Named: " + clientId.value());
        }
    }

}
