package org.quartz.utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implementations of this interface used by <code>DBConnectionManager</code>
 * to provide connections from various sources.
 *
 * @author Mohammad Rezaei
 * @see DBConnectionManager
 * @see PoolingConnectionProvider
 * @see JNDIConnectionProvider
 * @see org.quartz.utils.weblogic.WeblogicConnectionProvider
 */
public interface ConnectionProvider {

    /**
     * @return connection managed by this provider
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    void shutdown() throws SQLException;

    void initialize() throws SQLException;
}
