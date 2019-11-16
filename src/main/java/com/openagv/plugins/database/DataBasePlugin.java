package com.openagv.plugins.database;

import com.duangframework.db.DbClientFatory;
import com.duangframework.db.core.IDbClient;
import com.openagv.core.interfaces.IPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库插件
 *
 * @author Laotang
 */
public class DataBasePlugin implements IPlugin {

    private static final Logger LOG = LoggerFactory.getLogger(DataBasePlugin.class);

    private static List<IDbClient> DATABASE_CLIENT_LIST = new ArrayList<>();

    public DataBasePlugin(IDbClient dbClient) {
        java.util.Objects.requireNonNull(dbClient, "dbClient is null");
        DATABASE_CLIENT_LIST.add(dbClient);
    }

    public DataBasePlugin(List<IDbClient> dbClients) {
        java.util.Objects.requireNonNull(dbClients, "dbClients is null");
        DATABASE_CLIENT_LIST.addAll(dbClients);
    }

    @Override
    public void start() throws Exception {
        for (IDbClient<?> dbClient : DATABASE_CLIENT_LIST) {
            dbClient.getClient();
            DbClientFatory.CLIENT_MAP.put(dbClient.getClientId(), dbClient);
            printInfo(dbClient);
        }
    }

    private void printInfo(IDbClient<?> dbClient) {
        LOG.info("链接数据库"+ dbClient.getOptions().getDataBase()+"成功" +
                "，Client Id：" + dbClient.getClientId() +
                "，链接地址："+ dbClient.getOptions().getHost()+":"+ dbClient.getOptions().getPort());
    }

    public void stop() throws Exception {
        for (IDbClient<?> dbClient : DATABASE_CLIENT_LIST) {
            DbClientFatory.CLIENT_MAP.remove(dbClient.getClientId());
            dbClient.close();
        }
    }

}
