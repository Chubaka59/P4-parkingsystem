package com.parkit.parkingsystem.integration.config;

import com.parkit.parkingsystem.config.DataBaseConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DataBaseTestConfig extends DataBaseConfig {

    private static final Logger logger = LogManager.getLogger("DataBaseTestConfig");
    private static final String DB_URL_PROP_KEY = "dbUrl";
    private static final String DB_USERNAME_PROP_KEY = "dbUsername";
    private static final String DB_PASSWORD_PROP_KEY = "dbPassword";

    public Connection getConnection() throws ClassNotFoundException, SQLException, IOException {
        logger.info("Create DB connection");
        String fileLocation = "src/main/resources/TestDatabaseCredentials.property";
        Properties dbProperties = new Properties();
        dbProperties.load(new FileReader(fileLocation));
        String dbUrl = dbProperties.getProperty(DB_URL_PROP_KEY);
        String username = dbProperties.getProperty(DB_USERNAME_PROP_KEY);
        String password = dbProperties.getProperty(DB_PASSWORD_PROP_KEY);
        dbProperties.remove(DB_URL_PROP_KEY);
        dbProperties.remove(DB_USERNAME_PROP_KEY);
        dbProperties.remove(DB_PASSWORD_PROP_KEY);
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(dbUrl, username, password);
    }

    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                logger.info("Closing DB connection");
            } catch (SQLException e) {
                logger.error("Error while closing connection", e);
            }
        }
    }

    public void closePreparedStatement(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.close();
                logger.info("Closing Prepared Statement");
            } catch (SQLException e) {
                logger.error("Error while closing prepared statement", e);
            }
        }
    }

    public void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                logger.info("Closing Result Set");
            } catch (SQLException e) {
                logger.error("Error while closing result set", e);
            }
        }
    }
}
