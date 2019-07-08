package org.quartz.utils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * <p>
 * Manages a collection of ConnectionProviders, and provides transparent access
 * to their connections.
 * </p>
 * 
 * @see ConnectionProvider
 * @see PoolingConnectionProvider
 * @see JNDIConnectionProvider
 * @see org.quartz.utils.weblogic.WeblogicConnectionProvider
 * 
 * @author James House
 * @author Sharada Jambula
 * @author Mohammad Rezaei
 */
public class DBConnectionManager {

    public static final String DB_PROPS_PREFIX = "org.quartz.db.";

    private static DBConnectionManager instance = new DBConnectionManager();

    private HashMap<String, ConnectionProvider> providers = new HashMap<String, ConnectionProvider>();


    /**
     * <p>
     * Private constructor
     * </p>
     *  
     */
    private DBConnectionManager() {
    }

    public void addConnectionProvider(String dataSourceName,
            ConnectionProvider provider) {
        this.providers.put(dataSourceName, provider);
    }

    /**
     * Get a database connection from the DataSource with the given name.
     * 
     * @return a database connection
     * @exception SQLException
     *              if an error occurs, or there is no DataSource with the
     *              given name.
     */
    public Connection getConnection(String dsName) throws SQLException {
        ConnectionProvider provider = providers.get(dsName);
        if (provider == null) {
            throw new SQLException("There is no DataSource named '"
                    + dsName + "'");
        }

        return provider.getConnection();
    }

    /**
     * Get the class instance.
     * 
     * @return an instance of this class
     */
    public static DBConnectionManager getInstance() {
        // since the instance variable is initialized at class loading time,
        // it's not necessary to synchronize this method */
        return instance;
    }

    /**
     * Shuts down database connections from the DataSource with the given name,
     * if applicable for the underlying provider.
     *
     * @exception SQLException
     *              if an error occurs, or there is no DataSource with the
     *              given name.
     */
    public void shutdown(String dsName) throws SQLException {

        ConnectionProvider provider = (ConnectionProvider) providers
        .get(dsName);
        if (provider == null) {
            throw new SQLException("There is no DataSource named '"
                    + dsName + "'");
        }

        provider.shutdown();

    }

    ConnectionProvider getConnectionProvider(String key) {
        return providers.get(key);
    }
}
