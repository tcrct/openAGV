package com.openagv.db.mongodb;

import com.openagv.db.IDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoBaseDao<T> implements IDao<T> {

    private final static Logger logger = LoggerFactory.getLogger(MongoBaseDao.class);

    protected Class<?> cls;

    public MongoBaseDao() {

    }

    public MongoBaseDao(Class<T> cls){
        this.cls = cls;
    }

    @Override
    public <T> T save(T entity) throws Exception {
        System.out.println("entity.getClass().getSimpleName(): " + entity.getClass().getSimpleName());
        return null;
    }

}
