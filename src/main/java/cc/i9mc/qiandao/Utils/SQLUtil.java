package cc.i9mc.qiandao.Utils;

import cc.i9mc.qiandao.Qiandao;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class SQLUtil {

    private static SQLUtil databaseManager;
    private BoneCP connectionPool;

    private SQLUtil() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        try {
            BoneCPConfig config = new BoneCPConfig();
            config.setJdbcUrl("jdbc:mysql://" + Qiandao.ip + "/" + Qiandao.database);
            config.setUsername(Qiandao.username);
            config.setPassword(Qiandao.password);
            config.setMinConnectionsPerPartition(5);
            config.setMaxConnectionsPerPartition(10);
            config.setPartitionCount(1);
            config.setMaxConnectionAge(3600, TimeUnit.SECONDS);
            connectionPool = new BoneCP(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static SQLUtil getInstance() {
        if (databaseManager == null)
            databaseManager = new SQLUtil();
        return databaseManager;
    }

    public Connection getConnection() throws SQLException {
        return this.connectionPool.getConnection();
    }

}
