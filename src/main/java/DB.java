import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.swing.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class DB {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String DB_CONNECTION_URL = "jdbc:mysql://localhost:3306/MusicStore";
    private static final String USER = "alyssa";
    private static final String PASSWORD = "kittens";

    final static String CONSIGNOR_TABLE_NAME = "Consignors";
    final static String CONSIGNOR_NUMBER_COLUMN_NAME = "ConsignorNum";
    final static String LAST_NAME_COLUMN_NAME = "LastName";
    final static String FIRST_NAME_COLUMN_NAME = "FirstName";
    final static String PHONE_COLUMN_NAME = "Phone";
    final static String AMOUNT_OWED_COLUMN_NAME = "AmountOwed";
    final static String PROFIT_COLUMN_NAME = "Profit";
    final static String RECORDS_TABLE_NAME = "Records";
    final static String RECORD_NUMBER_COLUMN_NAME = "RecordNum";
    final static String ARTIST_COLUMN_NAME = "Artist";
    final static String TITLE_COLUMN_NAME = "Title";
    final static String PRICE_COLUMN_NAME = "Price";
    final static String DATE_RECEIVED_COLUMN_NAME = "DateReceived";
    final static String DAYS_IN_STORE_COLUMN_NAME = "DaysInStore";
    final static String LOCATION_COLUMN_NAME = "Location";
    final static String SALES_TABLE_NAME = "Sales";
    final static String STORE_CUT_COLUMN_NAME = "StoreCut";
    final static String SALE_PRICE_COLUMN_NAME = "SalePrice";
    final static String SALE_DATE_COLUMN_NAME = "SaleDate";
    final static String SALE_NUMBER_COLUMN_NAME = "SaleNum";

    final static double CONSIGNOR_CUT = 0.4;
    final static int MAX_NUM_COPIES_OF_RECORD = 50;
    final static int DAYS_TO_MOVE_TO_BARGAIN_BASEMENT = 30;
    final static int DAYS_TO_DONATE = 365;
    final static double BARGAIN_BASEMENT_PRICE = 1.0;

    // overBargainBasementDays and overDonateDays hold record numbers only
    static ArrayList<Integer> overBargainBasementDays = new ArrayList();
    static ArrayList<String> bargainBasementDetails = new ArrayList<>();
    static ArrayList<Integer> overDonateDays = new ArrayList();
    static ArrayList<String> donateDetails = new ArrayList<>();
    static ArrayList<String> consignors = new ArrayList();
    static ArrayList<String> record = new ArrayList<>();
    static ArrayList<String> overNumCopies = new ArrayList<>();
    static ArrayList<String> recordInfo = new ArrayList();

    DecimalFormat format = new DecimalFormat("0.00");

    DB() {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException cnfe) {
            System.out.println("Error finding JDBC driver. Printing stack trace.");
            cnfe.printStackTrace();
            System.exit(1);
        }
    }

    // creates tables: Consignors, Records, and Sales if they don't already exist
    boolean setup() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            if (!consignorsTableExists()) {
                String createTableSQL = "CREATE TABLE " + CONSIGNOR_TABLE_NAME + " (" + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + LAST_NAME_COLUMN_NAME + " varchar(30), " + FIRST_NAME_COLUMN_NAME + " varchar(30), " + PHONE_COLUMN_NAME + " varchar(15) NOT NULL, " + AMOUNT_OWED_COLUMN_NAME + " double, " + PROFIT_COLUMN_NAME + " double, PRIMARY KEY(" + CONSIGNOR_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTableSQL);
            } if (!recordsTableExists()) {
                String createTableSQL = "CREATE TABLE " + RECORDS_TABLE_NAME + " (" + RECORD_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL, " + ARTIST_COLUMN_NAME + " varchar(30), " + TITLE_COLUMN_NAME + " varchar(30), " + PRICE_COLUMN_NAME + " double, " + DATE_RECEIVED_COLUMN_NAME + " varchar(30), " + DAYS_IN_STORE_COLUMN_NAME + " int, " + LOCATION_COLUMN_NAME + " varchar(10), PRIMARY KEY(" + RECORD_NUMBER_COLUMN_NAME + "), FOREIGN KEY(" + CONSIGNOR_NUMBER_COLUMN_NAME + ") REFERENCES " + CONSIGNOR_TABLE_NAME + "(" + CONSIGNOR_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTableSQL);
            } if (!salesTableExists()) {
                String createTablesSQL = "CREATE TABLE " + SALES_TABLE_NAME + " (" + SALE_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + RECORD_NUMBER_COLUMN_NAME + " int NOT NULL, " + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL, " + ARTIST_COLUMN_NAME + " varchar(30), " + TITLE_COLUMN_NAME + " varchar(30), " + SALE_PRICE_COLUMN_NAME + " double, " + STORE_CUT_COLUMN_NAME + " double, " + SALE_DATE_COLUMN_NAME + " varchar(30), PRIMARY KEY (" + SALE_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTablesSQL);
            }
            statement.close();
            conn.close();
            return true;
        } catch (SQLException se) {
            System.out.println("There was a problem with the database and/or tables. Printing stack trace and quitting program.");
            se.printStackTrace();
            return false;
        }
    }

    // Checks if the Consignors table already exists in the DB
    static boolean consignorsTableExists() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String checkTablePresentQuery = "SHOW TABLES LIKE '" + CONSIGNOR_TABLE_NAME + "'";
            ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
            boolean exists = tablesRS.next();
            tablesRS.close();
            statement.close();
            conn.close();
            return exists;
        } catch (SQLException sqle){
            System.out.println("There was an error finding the Consignors table. Printing stack trace.");
            sqle.printStackTrace();
            return false;
        }
    }

    // Checks if the Records table already exists in the DB
    static boolean recordsTableExists() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String checkTablePresentQuery = "SHOW TABLES LIKE '" + RECORDS_TABLE_NAME + "'";
            ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
            boolean exists = tablesRS.next();
            tablesRS.close();
            statement.close();
            conn.close();
            return exists;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding the Records table. Printing stack trace.");
            sqle.printStackTrace();
            return false;
        }
    }

    // Checks if the Sales table already exists in the DB
    static boolean salesTableExists() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String checkTablePresentQuery = "SHOW TABLES LIKE '" + SALES_TABLE_NAME + "'";
            ResultSet tablesRS = statement.executeQuery(checkTablePresentQuery);
            boolean exists = tablesRS.next();
            tablesRS.close();
            statement.close();
            conn.close();
            return exists;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding the Sales table. Printing stack trace.");
            sqle.printStackTrace();
            return false;
        }
    }

    // Updates the total number of days in store for each record
    void updateDaysInStore() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             Statement stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String getDatesReceivedSQL = "SELECT " + DATE_RECEIVED_COLUMN_NAME + ", " + RECORD_NUMBER_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME;
            ResultSet rs = statement.executeQuery(getDatesReceivedSQL);
            LocalDate today = LocalDate.now();
            while (rs.next()) {
                LocalDate date1 = LocalDate.parse(rs.getString(DATE_RECEIVED_COLUMN_NAME));
                int days = Days.daysBetween(date1, today).getDays();
                String updateDaysInStoreSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + DAYS_IN_STORE_COLUMN_NAME + "=" + days + " WHERE " + RECORD_NUMBER_COLUMN_NAME + "=" + rs.getString(RECORD_NUMBER_COLUMN_NAME);
                stat.executeUpdate(updateDaysInStoreSQL);
            }
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error updating the number of days in-store. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // If any records need to be moved to the basement or donated, the user is alerted
    void alertDaysInStore() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            overBargainBasementDays.clear();
            overDonateDays.clear();
            bargainBasementDetails.clear();
            donateDetails.clear();
            String findDaysBasementSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + DAYS_IN_STORE_COLUMN_NAME + " >= " + DAYS_TO_MOVE_TO_BARGAIN_BASEMENT;
            ResultSet rsBasement1 = statement.executeQuery(findDaysBasementSQL);
            while (rsBasement1.next()) {
                if (rsBasement1.getString(LOCATION_COLUMN_NAME) != "Basement") {
                    overBargainBasementDays.add(rsBasement1.getInt(RECORD_NUMBER_COLUMN_NAME));
                    bargainBasementDetails.add("\nConsignorNum: " + rsBasement1.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + " Record: " + rsBasement1.getString(ARTIST_COLUMN_NAME) + ", " + rsBasement1.getString(TITLE_COLUMN_NAME) + " located at " + rsBasement1.getString(LOCATION_COLUMN_NAME));
                }
            }
            for (int x = 0; x < overBargainBasementDays.size(); x++) {
                String updatePriceSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + PRICE_COLUMN_NAME + " = " + BARGAIN_BASEMENT_PRICE + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = " + overBargainBasementDays.get(x);
                statement.executeUpdate(updatePriceSQL);
            }

            if (!overBargainBasementDays.isEmpty()) {
                ResultSet rsBasement2 = statement.executeQuery(findDaysBasementSQL);
                while (rsBasement2.next()) {
                    String artist = rsBasement2.getString(ARTIST_COLUMN_NAME);
                    String title = rsBasement2.getString(TITLE_COLUMN_NAME);
                    String location = rsBasement2.getString(LOCATION_COLUMN_NAME);
                    ArrayList<String> albumsInfo = new ArrayList<>();
                    albumsInfo.add(artist + ", " + title + " at " + location);
                }
                JOptionPane.showMessageDialog(null, "Attention! These records have been in the system for " + DAYS_TO_MOVE_TO_BARGAIN_BASEMENT + " days, it is time to alert the consignor or move them to the bargain basement and change price to $" + BARGAIN_BASEMENT_PRICE +
                        ":\nRecordNum(s): " + overBargainBasementDays.toString() + "\nRecord Details:" + bargainBasementDetails);
                rsBasement2.close();
            }

            String findDaysDonateSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + DAYS_IN_STORE_COLUMN_NAME + " >= " + DAYS_TO_DONATE;
            ResultSet rsDonate = statement.executeQuery(findDaysDonateSQL);
            String findDaysSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + DAYS_IN_STORE_COLUMN_NAME + " >= " + DAYS_TO_MOVE_TO_BARGAIN_BASEMENT;
            Statement stat = conn.createStatement();
            ResultSet rsBasement3 = stat.executeQuery(findDaysSQL);
            while (rsDonate.next()) {
                overDonateDays.add(rsDonate.getInt(RECORD_NUMBER_COLUMN_NAME));
            }
            while (rsBasement3.next()) {
                donateDetails.add("\nConsignorNum: " + rsBasement3.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + " Record: " + rsBasement3.getString(ARTIST_COLUMN_NAME) + ", " + rsBasement3.getString(TITLE_COLUMN_NAME) + " located at " + rsBasement3.getString(LOCATION_COLUMN_NAME));
            }

            if (!overDonateDays.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Attention! These records have been in the system for at least " + DAYS_TO_DONATE + " days, it is time to contact the consignors and donate them:\nRecord Num(s): " + overDonateDays.toString() + "\nRecord Details:" + donateDetails);
            }
            for (int x = 0; x < overBargainBasementDays.size(); x++) {
                String updateLocationSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + LOCATION_COLUMN_NAME + " = 'Basement' WHERE " + RECORD_NUMBER_COLUMN_NAME + " = " + overBargainBasementDays.get(x);
                statement.executeUpdate(updateLocationSQL);
            }
            rsBasement1.close();
            rsDonate.close();
            rsBasement3.close();
            stat.close();
            statement.close();
            conn.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error looking up records' number of days in-store. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Checks if there are too many of any album and alerts the user
    void checkNumCopiesMax() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
             Statement stat = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            overNumCopies.clear();
            String getAllRecordsSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " ORDER BY " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME;
            ResultSet allRecords = statement.executeQuery(getAllRecordsSQL);
            while (allRecords.next()) {
                String artist = allRecords.getString(ARTIST_COLUMN_NAME);
                String title = allRecords.getString(TITLE_COLUMN_NAME);
                String getAllInfoSQL = "SELECT " + LOCATION_COLUMN_NAME + ", COUNT(" + RECORD_NUMBER_COLUMN_NAME + ") FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + " = '" + artist + "' AND " + TITLE_COLUMN_NAME + " = '" + title + "'";
                ResultSet rs = stat.executeQuery(getAllInfoSQL);
                rs.next();
                int numCopies = rs.getInt("COUNT(" + RECORD_NUMBER_COLUMN_NAME + ")");
                if (numCopies >= MAX_NUM_COPIES_OF_RECORD) {
                    if (!overNumCopies.contains("\n" + artist + ", " + title + ", located at " + rs.getString(LOCATION_COLUMN_NAME))) {
                        overNumCopies.add("\n" + artist + ", " + title + ", located at " + rs.getString(LOCATION_COLUMN_NAME));
                    }
                }
                rs.close();
            }
            if (!overNumCopies.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Attention, you have the maximum number of records for the following titles: \n" + overNumCopies);
                overNumCopies.clear();
            }
            allRecords.close();
            statement.close();
            conn.close();
        } catch (SQLException sqle){
            System.out.println("There was an error checking the number of copies of each record. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Populates the consignors list from the database
    ArrayList populateConsignorList() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD)) {
            consignors.clear();
            String populateConsignorsSQL = "SELECT " + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " ORDER BY " + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME;
            PreparedStatement populateConsignorList = conn.prepareStatement(populateConsignorsSQL);
            ResultSet rs = populateConsignorList.executeQuery();
            while (rs.next()) {
                String lastName = rs.getString(LAST_NAME_COLUMN_NAME);
                String firstName = rs.getString(FIRST_NAME_COLUMN_NAME);
                consignors.add(lastName + ", " + firstName);
            }
            rs.close();
            populateConsignorList.close();
            conn.close();
            return consignors;
        } catch (SQLException sqle){
            System.out.println("There was an error populating the Consignors list. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Populates the records list from the database
    ArrayList populateRecordList() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            record.clear();
            String populateRecordsSQL = "SELECT " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " ORDER BY " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME;
            ResultSet rs = statement.executeQuery(populateRecordsSQL);
            while (rs.next()) {
                String artist = rs.getString(ARTIST_COLUMN_NAME);
                String title = rs.getString(TITLE_COLUMN_NAME);
                if (!record.contains(artist + ", " + title)) {
                    record.add(artist + ", " + title);
                }
            }
            rs.close();
            statement.close();
            conn.close();
            return record;
        } catch (SQLException sqle){
            System.out.println("There was an error populating the Records list. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Creates ArrayList with all ConsignorNums in database
    ArrayList updateComboBox() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ArrayList<Integer> comboBoxItems = new ArrayList();
            String getAllConsignorNumsSQL = "SELECT " + CONSIGNOR_NUMBER_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME;
            ResultSet allConsignorNums = statement.executeQuery(getAllConsignorNumsSQL);
            while (allConsignorNums.next()) {
                comboBoxItems.add(allConsignorNums.getInt(CONSIGNOR_NUMBER_COLUMN_NAME));
            }
            allConsignorNums.close();
            statement.close();
            conn.close();
            return comboBoxItems;
        } catch (SQLException sqle) {
            System.out.println("There was an error updating the combo boxes. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Returns a consignor's first and last name in JList format
    String findConsignorGivenNum(int number) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD)) {
            String lookupConsignorSQL = "SELECT " + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement findConsignor = conn.prepareStatement(lookupConsignorSQL);
            findConsignor.setInt(1, number);
            ResultSet rs = findConsignor.executeQuery();
            rs.next();
            String lastname = rs.getString(LAST_NAME_COLUMN_NAME);
            String firstname = rs.getString(FIRST_NAME_COLUMN_NAME);
            findConsignor.close();
            rs.close();
            conn.close();
            return (lastname + ", " + firstname);
        } catch (SQLException sqle) {
            System.out.println("There was an error finding a consignor. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Returns a record's artist and title in Jlist format
    static String findRecordGivenNum(int number) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD)) {
            String lookupRecordSQL = "SELECT " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement findRecord = conn.prepareStatement(lookupRecordSQL);
            findRecord.setInt(1, number);
            ResultSet rs = findRecord.executeQuery();
            rs.next();
            String artist = rs.getString(ARTIST_COLUMN_NAME);
            String title = rs.getString(TITLE_COLUMN_NAME);
            findRecord.close();
            rs.close();
            conn.close();
            return (artist + ", " + title);
        } catch (SQLException sqle) {
            System.out.println("There was an error finding a record. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Checks if a certain record exists
    static boolean recordExists(String userEntry) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);) {
            String lookupViewAllRecordsSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement checkRecordPresent = conn.prepareStatement(lookupViewAllRecordsSQL);
            checkRecordPresent.setInt(1, Integer.parseInt(userEntry));
            ResultSet recordsRS = checkRecordPresent.executeQuery();
            boolean exists = recordsRS.next();
            recordsRS.close();
            checkRecordPresent.close();
            conn.close();
            return exists;
        }
        catch (SQLException sqle) {
            System.out.println("There was an error finding a record. Printing stack trace.");
            sqle.printStackTrace();
            return false;
        }
    }

    // Checks the number of copies of a record in database
    void checkNumCopies(String artist, String title) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);) {
            String getAllInfoSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + "= ? AND " + TITLE_COLUMN_NAME + "= ?";
            PreparedStatement ps = conn.prepareStatement(getAllInfoSQL);
            ps.setString(1, artist);
            ps.setString(2, title);
            ResultSet rs = ps.executeQuery();
            rs.last();
            int numRows = rs.getRow();
            if (numRows >= MAX_NUM_COPIES_OF_RECORD) {
                JOptionPane.showMessageDialog(null, "Attention: You have the maximum number of albums of this title: " + artist + ", " + title);
            }
            rs.close();
            ps.close();
            conn.close();
        } catch (SQLException sqle){
            System.out.println("There was an error checking the number of copies of a record. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Adds a consignor to the database
    void addConsignor(String last, String first, String phone) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);) {
            String addConsignorSQL = "INSERT INTO " + CONSIGNOR_TABLE_NAME + "(" + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + ", " + PHONE_COLUMN_NAME + ", " + AMOUNT_OWED_COLUMN_NAME + ", " + PROFIT_COLUMN_NAME + ") VALUES (?, ?, ?, 0.0, 0.0)";
            PreparedStatement addConsignor = conn.prepareStatement(addConsignorSQL);
            addConsignor.setString(1, last);
            addConsignor.setString(2, first);
            addConsignor.setString(3, phone);
            addConsignor.executeUpdate();
            addConsignor.close();
            conn.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error adding a consignor. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Adds a record to the database
    void addRecord(int consignor, String artist, String title, double price, String location) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);) {
            String addRecordSQL = "INSERT INTO " + RECORDS_TABLE_NAME + " (" + CONSIGNOR_NUMBER_COLUMN_NAME + ", " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + ", " + PRICE_COLUMN_NAME + ", " + DATE_RECEIVED_COLUMN_NAME + ", " + DAYS_IN_STORE_COLUMN_NAME + ", " + LOCATION_COLUMN_NAME + ") VALUES (?, ?, ?, ?, ?, 0, ?)";
            PreparedStatement addRecord = conn.prepareStatement(addRecordSQL);
            addRecord.setInt(1, consignor);
            addRecord.setString(2, artist);
            addRecord.setString(3, title);
            addRecord.setDouble(4, price);
            addRecord.setString(5, LocalDate.now().toString());
            addRecord.setString(6, location);
            addRecord.executeUpdate();
            checkNumCopies(artist, title);
            addRecord.close();
            conn.close();
        } catch (SQLException sqle){
            System.out.println("There was an error adding a record. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Removes a consignor from the database
    void removeConsignor(int consignor) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);) {
            String deleteSQL = "DELETE FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement deleteConsignor = conn.prepareStatement(deleteSQL);
            deleteConsignor.setInt(1, consignor);
            deleteConsignor.executeUpdate();
            deleteConsignor.close();
            conn.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error deleting a consignor. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Removes a record from the database
    void removeRecord(int recordNum, double price) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {

            // Getting the consignor number attached to the record sold
            String selectConsignorSQL = "SELECT " + CONSIGNOR_NUMBER_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement getCurrentConsignor = conn.prepareStatement(selectConsignorSQL);
            getCurrentConsignor.setInt(1, recordNum);
            ResultSet currentConsignorRS = getCurrentConsignor.executeQuery();
            currentConsignorRS.next();
            String currentConsignorNum = currentConsignorRS.getString(CONSIGNOR_NUMBER_COLUMN_NAME);

            // Getting the current amount owed to the consignor attached to the sold record
            String selectProfitSQL = "SELECT " + AMOUNT_OWED_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement getCurrentAmountOwed = conn.prepareStatement(selectProfitSQL);
            getCurrentAmountOwed.setString(1, currentConsignorNum);
            ResultSet consignorCurrentProfitRS = getCurrentAmountOwed.executeQuery();
            consignorCurrentProfitRS.next();

            // Calculating consignor's cut and store's cut
            double consignorCurrentAmountOwed = consignorCurrentProfitRS.getDouble(AMOUNT_OWED_COLUMN_NAME);
            double consignorCut = price * CONSIGNOR_CUT;
            double storeCut = price - consignorCut;
            consignorCurrentAmountOwed += consignorCut;

            // Updating consignor's amount owed
            String updateSQL = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + AMOUNT_OWED_COLUMN_NAME + " = " + consignorCurrentAmountOwed + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + currentConsignorNum;
            statement.executeUpdate(updateSQL);

            // Finding the full record name (artist & title) and deleting it from the database
            String fullAlbum = DB.findRecordGivenNum(recordNum);
            String deleteSQL = "DELETE FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
            PreparedStatement deleteRecord = conn.prepareStatement(deleteSQL);
            deleteRecord.setInt(1, recordNum);
            deleteRecord.executeUpdate();

            // Extracting the title and artist of the record sold, searching database to see if there are still copies left in the store, if so don't delete the record from the JList & vice versa
            String title = fullAlbum.substring(fullAlbum.indexOf(",") + 2);
            String artist = fullAlbum.substring(0, fullAlbum.indexOf(","));

            // Adding the information about the record sold to the Sales table (assumes the record came into the store the same day the record is being created in the DB)
            String insertSalesSQL = "INSERT INTO " + SALES_TABLE_NAME + " (" + RECORD_NUMBER_COLUMN_NAME + " , " + CONSIGNOR_NUMBER_COLUMN_NAME + ", " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + ", " + SALE_PRICE_COLUMN_NAME + " , " + STORE_CUT_COLUMN_NAME + ", " + SALE_DATE_COLUMN_NAME + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement insertSales = conn.prepareStatement(insertSalesSQL);
            insertSales.setInt(1, recordNum);
            insertSales.setInt(2, Integer.parseInt(currentConsignorNum));
            insertSales.setString(3, artist);
            insertSales.setString(4, title);
            insertSales.setDouble(5, price);
            insertSales.setDouble(6, storeCut);
            insertSales.setString(7, DateTime.now().toString());
            insertSales.executeUpdate();

            conn.close();
            consignorCurrentProfitRS.close();
            currentConsignorRS.close();
            getCurrentConsignor.close();
            getCurrentAmountOwed.close();
            deleteRecord.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error deleting a record. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Returns a consignor's ID number
    int clickConsignor(String consignor) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String fullName = consignor;
            String first = fullName.substring(fullName.indexOf(",") + 2);
            String last = fullName.substring(0, fullName.indexOf(","));
            String selectAllTwoVarSQL = "SELECT " + CONSIGNOR_NUMBER_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + LAST_NAME_COLUMN_NAME + " = '" + last + "' AND " + FIRST_NAME_COLUMN_NAME + " = '" + first + "'";
            ResultSet rsConsignor = statement.executeQuery(selectAllTwoVarSQL);
            rsConsignor.next();
            int consignorNum = rsConsignor.getInt(CONSIGNOR_NUMBER_COLUMN_NAME);
            rsConsignor.close();
            statement.close();
            conn.close();
            return consignorNum;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding consignor information. Printing stack trace.");
            sqle.printStackTrace();
            return 0;
        }
    }

    // Returns an ArrayList of records associated with a particular consignor
    ArrayList<String> findAssociatedRecords(int consignorNum) {
        ArrayList<String> associatedRecords = new ArrayList();
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String selectAssociatedRecordsSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + consignorNum;
            ResultSet rsRecord = statement.executeQuery(selectAssociatedRecordsSQL);
            while (rsRecord.next()) {
                associatedRecords.add("\nRecordNum: " + rsRecord.getString(RECORD_NUMBER_COLUMN_NAME) + ", Artist: " + rsRecord.getString(ARTIST_COLUMN_NAME) + ", Title: " + rsRecord.getString(TITLE_COLUMN_NAME) + ", Location: " + rsRecord.getString(LOCATION_COLUMN_NAME));
            }
            rsRecord.close();
            conn.close();
            return associatedRecords;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding associated records. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Generates statistics from the database, such as average number of days in-store
    String getStatistics() {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String totalStoreEarningsSQL = "SELECT SUM(" + STORE_CUT_COLUMN_NAME + ") FROM " + SALES_TABLE_NAME;
            String averageDaysInStoreSQL = "SELECT AVG(" + DAYS_IN_STORE_COLUMN_NAME + ") FROM " + RECORDS_TABLE_NAME;
            ResultSet earningsRS = statement.executeQuery(totalStoreEarningsSQL);
            earningsRS.next();
            Double totalEarnings = earningsRS.getDouble("SUM(" + STORE_CUT_COLUMN_NAME + ")");
            ResultSet avgDaysRS = statement.executeQuery(averageDaysInStoreSQL);
            avgDaysRS.next();
            int avgDaysInStore = avgDaysRS.getInt("AVG(" + DAYS_IN_STORE_COLUMN_NAME + ")");
            earningsRS.close();
            avgDaysRS.close();
            statement.close();
            conn.close();
            return String.format("Total earnings: $" + format.format(totalEarnings) + "\nAverage number of days in-store: " + format.format(avgDaysInStore));
        } catch (SQLException sqle) {
            System.out.println("There was an error calculating statistics. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Updates a consignor's owed amount and profit
    void payAConsignor(int consignor, double payment) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String getConsignorInfoSQL = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + consignor;
            ResultSet rsConsignor = statement.executeQuery(getConsignorInfoSQL);
            rsConsignor.next();
            Double newAmountOwed = rsConsignor.getDouble(AMOUNT_OWED_COLUMN_NAME) - payment;
            if (newAmountOwed < 0.0) {
                JOptionPane.showMessageDialog(null, "Consignor's amount owed is already or will be at or below zero.");
            } else {
                Double newProfit = rsConsignor.getDouble(PROFIT_COLUMN_NAME) + payment;
                String updateAmountOwed = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + AMOUNT_OWED_COLUMN_NAME + " = " + newAmountOwed + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + consignor;
                statement.executeUpdate(updateAmountOwed);
                String updateProfit = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + PROFIT_COLUMN_NAME + " = " + newProfit + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + consignor;
                statement.executeUpdate(updateProfit);
                JOptionPane.showMessageDialog(null, "Amount Owed and Total Profit updated successfully");
            }
            rsConsignor.close();
            statement.close();
            conn.close();
        } catch (SQLException sqle){
            System.out.println("There was an error updating the consignor's payment. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Returns all information in the Consignors table about a certain consignor
    String getConsignorInfo(int consignor) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            if (consignor != 0) {
                String findAllInfoSQL = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + consignor;
                ResultSet consignorInfo = statement.executeQuery(findAllInfoSQL);
                consignorInfo.next();
                int id = consignorInfo.getInt(CONSIGNOR_NUMBER_COLUMN_NAME);
                String first = consignorInfo.getString(FIRST_NAME_COLUMN_NAME);
                String last = consignorInfo.getString(LAST_NAME_COLUMN_NAME);
                String phone = consignorInfo.getString(PHONE_COLUMN_NAME);
                double amtOwed = consignorInfo.getDouble(AMOUNT_OWED_COLUMN_NAME);
                double profit = consignorInfo.getDouble(PROFIT_COLUMN_NAME);
                String fullInfo = "ID: " + id + "\nName: " + first + " " + last + "\nPhone: " + phone + "\nAmount Owed: " + amtOwed + "\nTotal Profit: " + profit;
                consignorInfo.close();
                statement.close();
                conn.close();
                return fullInfo;
            }
        } catch (SQLException sqle) {
            System.out.println("There was an error finding information about the consignor. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
        return null;
    }

    // Returns an ArrayList containing all information about a certain record in the Records table
    ArrayList<String> clickRecord(String fullAlbum) {
        recordInfo.clear();
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String title = fullAlbum.substring(fullAlbum.indexOf(",") + 2);
            String artist = fullAlbum.substring(0, fullAlbum.indexOf(","));
            String getAllInfoSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + " = '" + artist + "' AND " + TITLE_COLUMN_NAME + " = '" + title + "'";
            ResultSet rs = statement.executeQuery(getAllInfoSQL);
            rs.last();
            int numRows = rs.getRow();
            rs.beforeFirst();
            while (rs.next()) {
                recordInfo.add(" Duplicates of this record: " + numRows + "\n ID Number: " + rs.getString(RECORD_NUMBER_COLUMN_NAME) + "\n Consignor Number: " +
                        rs.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + "\n Price: " + rs.getString(PRICE_COLUMN_NAME) + "\n Date received: " + rs.getString(DATE_RECEIVED_COLUMN_NAME) +
                        "\n Days in store: " + rs.getString(DAYS_IN_STORE_COLUMN_NAME) + "\n Location: " + rs.getString(LOCATION_COLUMN_NAME) + "\n");
            }
            rs.close();
            statement.close();
            conn.close();
            return recordInfo;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding record information. Printing stack trace.");
            sqle.printStackTrace();
            return null;
        }
    }

    // Returns the same things as the method above, but accepts arguments for use in search
    ArrayList<String> findRecordInfo(String artist, String title, boolean or) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ArrayList<String> recordInfo = new ArrayList();
            ResultSet rs = null;
            String startFindRecordSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE ";
            String endFindRecordSQL = "";
            if (!or && artist != null && title != null) {
                endFindRecordSQL = ARTIST_COLUMN_NAME + " LIKE '%" + artist + "%' AND " + TITLE_COLUMN_NAME + " LIKE '%" + title + "%'";
            } else if ((or && artist == null) || (!or && artist == null)) {
                endFindRecordSQL = TITLE_COLUMN_NAME + " LIKE '%" + title + "%'";
            } else if ((or && title == null) || (!or && title == null)) {
                endFindRecordSQL = ARTIST_COLUMN_NAME + " LIKE '%" + artist + "%'";
            } else {
                endFindRecordSQL = ARTIST_COLUMN_NAME + " LIKE '%" + artist + "%' OR " + TITLE_COLUMN_NAME + " LIKE '%" + title + "%'";
            }
            String findRecordSQL = startFindRecordSQL + endFindRecordSQL;
            rs = statement.executeQuery(findRecordSQL);
            while (rs.next()) {
                recordInfo.add("Artist, Title: " + rs.getString(ARTIST_COLUMN_NAME) + ", " + rs.getString(TITLE_COLUMN_NAME) + "\n ID Number: " + rs.getString(RECORD_NUMBER_COLUMN_NAME) + "\n Consignor Number: " +
                        rs.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + "\n Price: " + rs.getString(PRICE_COLUMN_NAME) + "\n Date received: " + rs.getString(DATE_RECEIVED_COLUMN_NAME) +
                        "\n Days in store: " + rs.getString(DAYS_IN_STORE_COLUMN_NAME) + "\n Location: " + rs.getString(LOCATION_COLUMN_NAME) + "\n");
            }
            statement.close();
            rs.close();
            conn.close();
            return recordInfo;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding record information. Printing stack trace");
            sqle.printStackTrace();
            return null;
        }
    }

    // Returns an ArrayList of information about a consignor that accepts arguments for use in search
    ArrayList<String> findConsignorInfo(String first, String last, String phone, boolean firstOrLast, boolean lastOrPhone, boolean phoneOrFirst) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            ArrayList<String> consignorInfo = new ArrayList();
            String startFindConsignorSQL = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE ";
            String endFindConsignorSQL = "";
            String firstLastAndOr = " AND ";
            String lastPhoneAndOr = " AND ";
            String phoneFirstAndOr = " AND ";
            if (firstOrLast) {
                firstLastAndOr = " OR ";
            }
            if (lastOrPhone) {
                lastPhoneAndOr = " OR ";
            }
            if (phoneOrFirst) {
                phoneFirstAndOr = " OR ";
            }
            if (first != null && last == null && phone == null) {
                endFindConsignorSQL = FIRST_NAME_COLUMN_NAME + " LIKE '%" + first + "%'";
            } else if (first == null && last != null && phone == null) {
                endFindConsignorSQL = LAST_NAME_COLUMN_NAME + " LIKE '%" + last + "%'";
            } else if (first == null && last == null && phone != null) {
                endFindConsignorSQL = PHONE_COLUMN_NAME + " LIKE '%" + phone + "%'";
            } else if (first != null && last != null && phone == null) {
                endFindConsignorSQL = FIRST_NAME_COLUMN_NAME + " LIKE '%" + first + "%'" + firstLastAndOr + LAST_NAME_COLUMN_NAME + " LIKE '%" + last + "%'";
            } else if (first == null && last != null && phone != null) {
                endFindConsignorSQL = LAST_NAME_COLUMN_NAME + " LIKE '%" + last + "%'" + lastPhoneAndOr + PHONE_COLUMN_NAME + " LIKE '%" + phone + "%'";
            } else if (first != null && last == null && phone != null) {
                endFindConsignorSQL = FIRST_NAME_COLUMN_NAME + " LIKE '%" + first + "%'" + phoneFirstAndOr + FIRST_NAME_COLUMN_NAME + " LIKE '%" + first + "%'";
            } else if (first != null && last != null && phone != null) {
                if (phoneOrFirst) {
                    endFindConsignorSQL = FIRST_NAME_COLUMN_NAME + " LIKE '%" + first + "%'" + firstLastAndOr + LAST_NAME_COLUMN_NAME + " LIKE '%" + last + "%' " + lastPhoneAndOr + PHONE_COLUMN_NAME + " LIKE '%" + phone + "%'";
                } else {
                    endFindConsignorSQL = LAST_NAME_COLUMN_NAME + " LIKE '%" + last + "%'" + lastPhoneAndOr + PHONE_COLUMN_NAME + " LIKE '%" + phone + "%' " + phoneFirstAndOr + FIRST_NAME_COLUMN_NAME + " LIKE '%" + first + "%'";
                }
            }
            String findConsignorSQL = startFindConsignorSQL + endFindConsignorSQL;
            ResultSet rs = statement.executeQuery(findConsignorSQL);
            while (rs.next()) {
                consignorInfo.add("ID Number: " + rs.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + "\nName: " + rs.getString(FIRST_NAME_COLUMN_NAME) + " " + rs.getString(LAST_NAME_COLUMN_NAME) + "\nPhone: " + rs.getString(PHONE_COLUMN_NAME) + "\nAmount owed: " + rs.getDouble(AMOUNT_OWED_COLUMN_NAME) + "\nTotal profit: " + rs.getDouble(PROFIT_COLUMN_NAME) + "\n");
            }
            statement.close();
            rs.close();
            conn.close();
            return consignorInfo;
        } catch (SQLException sqle) {
            System.out.println("There was an error finding consignor information. Printing stack trace");
            sqle.printStackTrace();
            return null;
        }
    }

    // Updating a consignor's information in the database
    void updateConsignor(int consignorNum, String newVariable, String toUpdate) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String updateSQL = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + toUpdate + " = '" + newVariable + "' WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + consignorNum;
            statement.executeUpdate(updateSQL);
            statement.close();
            conn.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error updating consignor information. Printing stack trace.");
            sqle.printStackTrace();
        }
    }

    // Updating a record's information in the database
    void updateRecord(int recordNum, String newVariable, String toUpdate) {
        try (Connection conn = DriverManager.getConnection(DB_CONNECTION_URL, USER, PASSWORD);
             Statement statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
            String updateSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + toUpdate + " = " + newVariable + " WHERE " + toUpdate + " = " + recordNum;
            statement.executeUpdate(updateSQL);
            statement.close();
            conn.close();
        } catch (SQLException sqle) {
            System.out.println("There was an error updating records. Printing stack trace.");
            sqle.printStackTrace();
        }
    }
}