package ru.timoxa0.GABot.handlers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import ru.timoxa0.GABot.models.MCUser;

import java.sql.*;

public class DBHandler {
    private final static Logger logger = LogManager.getLogger(DBHandler.class);
    private static DBHandler dbHandler = null;
    private final String connectionString;
    private final String username;
    private final String password;
    private final String table;
    private final String idColumn;
    private final String uuidColumn;
    private final String usernameColumn;
    private final String passwordColumn;

    public static synchronized DBHandler getDBHandler() {
        assert dbHandler != null;
        return dbHandler;
    }

    public static synchronized void createDBHandler(String connectionString, String username, String password, String table, String idColumn, String uuidColumn, String usernameColumn, String passwordColumn) {
        if (dbHandler == null) {
            dbHandler = new DBHandler(connectionString, username, password, table, idColumn, uuidColumn, usernameColumn, passwordColumn);
        }
    }

    private DBHandler(String connectionString, String username, String password, String table, String idColumn, String uuidColumn, String usernameColumn, String passwordColumn) {
        this.connectionString = connectionString;
        this.username = username;
        this.password = password;
        this.table = table;
        this.idColumn = idColumn;
        this.uuidColumn = uuidColumn;
        this.usernameColumn = usernameColumn;
        this.passwordColumn = passwordColumn;

        Connection conn = connect();
        if (conn != null) {
            logger.info("Successfully connected to database!");
            disconnect(conn);
        } else {
            logger.fatal("Unable to connect to database! Check database settings.");
            System.exit(1);
        }

    }

    private Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(
                    this.connectionString,
                    this.username, this.password
            );
            assert conn != null;
            logger.debug("Connected to database");
        } catch (SQLException e) {
            logger.fatal(e.getMessage());
        }
        return conn;
    }

    private void disconnect(Connection conn) {
        try {
            conn.close();
            logger.debug("Disconnected from database");
        } catch (SQLException e) {
            logger.fatal(e.getMessage());
        }
    }

    public MCUser getUserByID(String id) {
        Connection conn = connect();
        MCUser user = null;
        if (conn != null) {
            try {
                PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s=?",
                        this.table, this.idColumn));
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.first() || rs.next()) {
                    user = new MCUser(
                            rs.getString(this.idColumn),
                            rs.getString(this.usernameColumn),
                            rs.getString(this.passwordColumn),
                            rs.getString(this.uuidColumn),
                            true
                    );
                }
                disconnect(conn);
            } catch (SQLException e) {
                logger.fatal(e.getMessage());
            }
        }
        return user;
    }

    public MCUser getUserByName(String username) {
        Connection conn = connect();
        MCUser user = null;
        if (conn != null) {
            try {
                PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s=?",
                        this.table, this.usernameColumn));
                stmt.setString(1, username);
                ResultSet rs = stmt.executeQuery();
                if (rs.first() || rs.next()) {
                    user = new MCUser(
                            rs.getString(this.idColumn),
                            rs.getString(this.usernameColumn),
                            rs.getString(this.passwordColumn),
                            rs.getString(this.uuidColumn),
                            true
                    );
                }
                disconnect(conn);
            } catch (SQLException e) {
                logger.fatal(e.getMessage());
            }
        }
        return user;
    }

    public MCUser getUserByUUID(String uuid) {
        Connection conn = connect();
        MCUser user = null;
        if (conn != null) {
            try {
                PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s=?",
                        this.table, this.uuidColumn));
                stmt.setString(1, uuid);
                ResultSet rs = stmt.executeQuery();
                if (rs.first() || rs.next()) {
                    user = new MCUser(
                            rs.getString(this.idColumn),
                            rs.getString(this.usernameColumn),
                            rs.getString(this.passwordColumn),
                            rs.getString(this.uuidColumn),
                            true
                    );
                }
                disconnect(conn);
            } catch (SQLException e) {
                logger.fatal(e.getMessage());
            }
        }
        return user;
    }

    public boolean  checkForUser(String id) {
        Connection conn = connect();
        boolean result = false;
        if (conn != null) {
            try {
                PreparedStatement stmt = conn.prepareStatement(String.format("SELECT * FROM %s WHERE %s=?",
                        this.table, this.idColumn));
                stmt.setString(1, id);
                ResultSet rs = stmt.executeQuery();
                result = (rs.first() || rs.next());
                disconnect(conn);
            } catch (SQLException e) {
                logger.fatal(e.getMessage());
            }
        }
        return result;
    }

    public boolean updateUser(@NotNull MCUser user) {
        Connection conn = connect();
        assert conn != null;
        try {
            if (!checkForUser(user.getID())) {
                PreparedStatement stmt = conn.prepareStatement(String.format("INSERT INTO %s (%s) VALUES (?)",
                        this.table, this.idColumn));
                stmt.setString(1, user.getID());
                stmt.executeQuery();
            }
            PreparedStatement stmt = conn.prepareStatement(String.format("UPDATE %s SET %s = ?, %s = ?, %s = ? WHERE %s=?",
                    this.table, this.usernameColumn, this.passwordColumn, this.uuidColumn, this.idColumn));
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getUUID());
            stmt.setString(4, user.getID());
            stmt.executeQuery();
            disconnect(conn);
        } catch (SQLException e) {
            disconnect(conn);
            logger.fatal(e.getMessage());
            return false;
        }
        return true;
    }
}
