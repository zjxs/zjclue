package com.siyue.util;

import com.actionsoft.bpms.util.DBSql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

/**
 * Created by Administrator on 2018/10/30.
 */
public class ConnectionUtils {

    public static String getConnectionType(){
        Connection connection = null;
        try {
            connection = DBSql.open();
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            String driverName = databaseMetaData.getDriverName();
            if(driverName.contains("Oracle")){
                return "ORACLE";
            }else if(driverName.contains("PostgreSQL")){
                return "PGSQL";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBSql.close(connection);
        }
        return "";
    }
}
