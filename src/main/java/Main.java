// database: MusicStore

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Main {

    static Statement statement = null;
    static Connection conn = null;

    final static String CONSIGNOR_TABLE_NAME = "Consignors";
    final static String CONSIGNOR_NUMBER_COLUMN_NAME = "ConsignorNum";
    final static String LAST_NAME_COLUMN_NAME = "LastName";
    final static String FIRST_NAME_COLUMN_NAME = "FirstName";
    final static String PHONE_COLUMN_NAME = "Phone";
    final static String RECORDS_TABLE_NAME = "Records";
    final static String RECORD_NUMBER_COLUMN_NAME = "RecordNum";
    final static String PROFIT_COLUMN_NAME = "Profit";
    final static String ARTIST_COLUMN_NAME = "Artist";
    final static String TITLE_COLUMN_NAME = "Title";
    final static String PRICE_COLUMN_NAME = "Price";
    final static String DATE_RECEIVED_COLUMN_NAME = "DateReceived";
    final static String DAYS_IN_STORE_COLUMN_NAME = "DaysInStore";
    final static String LOCATION_COLUMN_NAME = "Location";
    final static String SALES_TABLE_NAME = "Sales";
    final static String SALE_PRICE_COLUMN_NAME = "SalePrice";
    final static String SALE_DATE_COLUMN_NAME = "SaleDate";

    public static void main(String[] args) {

    if (!setup()) {
        System.exit(1);
    }

    MusicStoreGUI gui = new MusicStoreGUI();
}

    // sets up driver, connection and statement. Also creates tables: Consignors and Records
    static boolean setup() {
        try {
            DBUtils.getDriver();
            conn = DBUtils.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            if (!consignorsTableExists()) {
                String createTableSQL = "CREATE TABLE " + CONSIGNOR_TABLE_NAME + " (" + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + LAST_NAME_COLUMN_NAME + " varchar(30), " + FIRST_NAME_COLUMN_NAME + " varchar(30), " + PHONE_COLUMN_NAME + " varchar(30), " + PROFIT_COLUMN_NAME + " double, PRIMARY KEY(" + CONSIGNOR_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTableSQL);
                System.out.println("Created Consignors table");
            } if (!recordsTableExists()) {
                String createTableSQL = "CREATE TABLE " + RECORDS_TABLE_NAME + " (" + RECORD_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL, " + ARTIST_COLUMN_NAME + " varchar(30), " + TITLE_COLUMN_NAME + " varchar(30), " + PRICE_COLUMN_NAME + " double, " + DATE_RECEIVED_COLUMN_NAME + " varchar(30), " + DAYS_IN_STORE_COLUMN_NAME + " int, " + LOCATION_COLUMN_NAME + " varchar(10), PRIMARY KEY(" + RECORD_NUMBER_COLUMN_NAME + "), FOREIGN KEY(" + CONSIGNOR_NUMBER_COLUMN_NAME + ") REFERENCES " + CONSIGNOR_TABLE_NAME + "(" + CONSIGNOR_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTableSQL);
                System.out.println("Created Records table");
            } if (!salesTableExists()) {
                String createTablesSQL = "CREATE TABLE " + SALES_TABLE_NAME + " (" + RECORD_NUMBER_COLUMN_NAME + " int NOT NULL, " + CONSIGNOR_NUMBER_COLUMN_NAME + " int, " + ARTIST_COLUMN_NAME + " varchar(30), " + TITLE_COLUMN_NAME + " varchar(30), " + SALE_PRICE_COLUMN_NAME + " double, " + SALE_DATE_COLUMN_NAME + " varchar(30), PRIMARY KEY (" + RECORD_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTablesSQL);
                System.out.println("Created Sales table");
            }
            return true;
        } catch (SQLException se) {
            System.out.println("There was a problem with the database and/or tables. Printing stack trace and quitting program...");
            se.printStackTrace();
            return false;
        }
    }

    private static boolean consignorsTableExists() throws SQLException {
        String checkTablePresentQuery = "SHOW TABLES LIKE 'Consignors'";
        ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
        return tablesRS.next();
    }

    private static boolean recordsTableExists() throws SQLException {
        String checkTablePresentQuery = "SHOW TABLES LIKE 'Records'";
        ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
        return tablesRS.next();
    }

    private static boolean salesTableExists() throws SQLException {
        String checkTablePresentQuery = "SHOW TABLES LIKE 'Sales'";
        ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
        return tablesRS.next();
    }
}