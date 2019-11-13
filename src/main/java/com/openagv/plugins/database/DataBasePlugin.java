package com.openagv.plugins.database;

import com.duangframework.db.DbClientFatory;
import com.duangframework.db.core.IClient;
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

    private static List<IClient> DATABASE_CLIENT_LIST = new ArrayList<>();

    public DataBasePlugin(IClient dbClient) {
        java.util.Objects.requireNonNull(dbClient, "dbClient is null");
        DATABASE_CLIENT_LIST.add(dbClient);
    }

    public DataBasePlugin(List<IClient> dbClients) {
        java.util.Objects.requireNonNull(dbClients, "dbClients is null");
        DATABASE_CLIENT_LIST.addAll(dbClients);
    }

    @Override
    public void start() throws Exception {
        for (IClient<?> dbClient : DATABASE_CLIENT_LIST) {
            dbClient.getClient();
            DbClientFatory.CLIENT_MAP.put(dbClient.getClientId(), dbClient);
            printInfo(dbClient);
        }
    }

    private void printInfo(IClient<?> dbClient) {
        LOG.info("链接数据库"+ dbClient.getOptions().getDataBase()+"成功" +
                "，Client Id：" + dbClient.getClientId() +
                "，链接地址："+ dbClient.getOptions().getHost()+":"+ dbClient.getOptions().getPort());
    }

    public void stop() throws Exception {
        for (IClient<?> dbClient : DATABASE_CLIENT_LIST) {
            DbClientFatory.CLIENT_MAP.remove(dbClient.getClientId());
            dbClient.close();
        }
    }

}
