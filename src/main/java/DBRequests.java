import java.sql.ResultSet;
import java.sql.SQLException;

// Requests to the database that involve no user entry and are not direct startup actions
public class DBRequests extends MusicStoreGUI{

    static boolean consignorsTableExists() throws SQLException {
        String checkTablePresentQuery = "SHOW TABLES LIKE 'Consignors'";
        ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
        return tablesRS.next();
    }

    static boolean recordsTableExists() throws SQLException {
        String checkTablePresentQuery = "SHOW TABLES LIKE 'Records'";
        ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
        return tablesRS.next();
    }

    static boolean salesTableExists() throws SQLException {
        String checkTablePresentQuery = "SHOW TABLES LIKE 'Sales'";
        ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
        return tablesRS.next();
    }
}