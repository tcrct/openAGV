package com.openagv.db.mongodb;

public class MongoDao<T> extends MongoBaseDao<T>  {

    public MongoDao(){
    }

    public MongoDao(Class<T> clazz) {
        super(clazz);
    }

//    public MongoDao(DB db, MongoDatabase database, Class<T> clazz) {
//        super(db, database, clazz);
//    }


}
