package com.sk.bds.datainsight.util;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.Map;

public class DataSourceManager {

    private static final String[] DRIVER = {
            "com.mysql.jdbc.Driver",
            "org.mariadb.jdbc.Driver",
            "org.apache.hive.jdbc.HiveDriver",
            "com.mysql.jdbc.Driver",
            "com.mysql.jdbc.Driver",
            "com.mysql.jdbc.Driver"
    };
    private static final String[] JDBC = {
            "mysql",
            "mariadb",
            "hive2",
            "mysql",
            "mysql",
            "mysql"
    };

    public static final String[] SOURCE_NAME = {
            "MYSQL",
            "MARIADB",
            "HIVE",
            "RDS",
            "FILE",
            "ICOS"
    };

    public final static int MYSQL = 0;
    public final static int MARIADB = 1;
    public final static int HIVE = 2;
    public final static int RDS = 3;
    public final static int FILE = 4;
    public final static int ICOS = 5;

    public static SingleConnectionDataSource getDataSource(String urlStr, String port, String dbName, String id, String pwd) throws Exception {
        return getDataSource(MYSQL, urlStr, port, dbName, id, pwd, null);
    }

    public static SingleConnectionDataSource getDataSource(int type, Map<String, Object> param) throws Exception {
        return getDataSource(type, param.get("host").toString(), param.get("port").toString(), param.get("dbName").toString(),
                param.get("id").toString(), param.get("pwd").toString(), param.get("ssl").toString());
    }

    public static SingleConnectionDataSource getDataSource(int type, String urlStr, String port, String id, String pwd, String ssl) throws Exception {
        return getDataSource(type, urlStr, port, null, id, pwd, ssl);
    }

    private static SingleConnectionDataSource getDataSource(int type, String urlStr, String port, String dbName, String id, String pwd, String ssl) throws Exception {
        String url = null;
        if (ssl != null) {
            switch(type) {
                case FILE:
                case ICOS:
                case MYSQL:
                    ssl = String.format("useSSL=%s", ssl);
                    break;
                case MARIADB:
                    ssl = String.format("useSSL=%s", ssl);
                    break;
                case HIVE:
                    ssl = String.format("ssl=%s", ssl);
                    break;
                case RDS:
                    ssl = String.format("useSSL=%s", ssl);
                    break;
            }
            if (dbName == null) {
                url = String.format("jdbc:%s://%s:%s?%s&useUnicode=true&characterEncoding=utf8&autoReconnect=true", JDBC[type], urlStr, port, ssl);
            } else {
                url = String.format("jdbc:%s://%s:%s/%s?%s&useUnicode=true&characterEncoding=utf8&autoReconnect=true", JDBC[type], urlStr, port, dbName, ssl);
            }
        } else {
            if (dbName == null) {
                url = String.format("jdbc:%s://%s:%s?useUnicode=true&characterEncoding=utf8&autoReconnect=true", JDBC[type], urlStr, port);
            } else {
                url = String.format("jdbc:%s://%s:%s/%s?useUnicode=true&characterEncoding=utf8&autoReconnect=true", JDBC[type], urlStr, port, dbName);
            }
        }
        SingleConnectionDataSource sds = new SingleConnectionDataSource(url, id, pwd, false);
        sds.setDriverClassName(DRIVER[type]);
        return sds;
    }
}
