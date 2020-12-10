package com.nyble.main;

import com.nyble.topics.Names;

import java.util.Properties;

public class AppSystem {
    public final static String SOURCE_JDBC_CONN_NAME = "data-source-name";
    public final static String BASE_URL = "base-url";
    public final static String CRM_KEY = "crm-key";
    public final static String EXTERNAL_SYSTEM_ID = "external-system-id";//"555";
    public final static String INTERNAL_SYSTEM_ID = "internal-system-id";

    public static final Properties rmc = new Properties();
    public static final Properties rrp = new Properties();

    static{
        rmc.put(SOURCE_JDBC_CONN_NAME, "rmc");
        rmc.put(BASE_URL, "http://10.100.1.7:81/api");
        rmc.put(CRM_KEY, "khtnh5md");
        rmc.put(EXTERNAL_SYSTEM_ID, "555");
        rmc.put(INTERNAL_SYSTEM_ID, Names.RMC_SYSTEM_ID+"");

        rrp.put(SOURCE_JDBC_CONN_NAME, "rrp");
        rrp.put(BASE_URL, "http://10.100.1.30:81/api");
        rrp.put(CRM_KEY, "ph7cl82y");
        rrp.put(EXTERNAL_SYSTEM_ID, "555");
        rrp.put(INTERNAL_SYSTEM_ID, Names.RRP_SYSTEM_ID+"");
    }
}
