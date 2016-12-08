import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

// Each of these methods is called upon when the program first starts
// Some can be called upon later as well
public class Startup extends MusicStoreGUI{

    final static int DAYS_TO_MOVE_TO_BARGAIN_BASEMENT = 30;
    final static int DAYS_TO_DONATE = 365;
    final static double BARGAIN_BASEMENT_PRICE = 1.00;
    static ArrayList<String> overNumCopies = new ArrayList<>();

    // sets up driver, connection and statement. Also creates tables: Consignors, Records and Sales
    static boolean setup() {
        try {
            DBUtils.getDriver();
            conn = DBUtils.getConnection();
            statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            if (!DBRequests.consignorsTableExists()) {
                String createTableSQL = "CREATE TABLE " + CONSIGNOR_TABLE_NAME + " (" + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + LAST_NAME_COLUMN_NAME + " varchar(30), " + FIRST_NAME_COLUMN_NAME + " varchar(30), " + PHONE_COLUMN_NAME + " varchar(15) NOT NULL, " + AMOUNT_OWED_COLUMN_NAME + " double, " + PROFIT_COLUMN_NAME + " double, PRIMARY KEY(" + CONSIGNOR_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTableSQL);
                System.out.println("Created Consignors table");
            } if (!DBRequests.recordsTableExists()) {
                String createTableSQL = "CREATE TABLE " + RECORDS_TABLE_NAME + " (" + RECORD_NUMBER_COLUMN_NAME + " int NOT NULL AUTO_INCREMENT, " + CONSIGNOR_NUMBER_COLUMN_NAME + " int NOT NULL, " + ARTIST_COLUMN_NAME + " varchar(30), " + TITLE_COLUMN_NAME + " varchar(30), " + PRICE_COLUMN_NAME + " double, " + DATE_RECEIVED_COLUMN_NAME + " varchar(30), " + DAYS_IN_STORE_COLUMN_NAME + " int, " + LOCATION_COLUMN_NAME + " varchar(10), PRIMARY KEY(" + RECORD_NUMBER_COLUMN_NAME + "), FOREIGN KEY(" + CONSIGNOR_NUMBER_COLUMN_NAME + ") REFERENCES " + CONSIGNOR_TABLE_NAME + "(" + CONSIGNOR_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTableSQL);
                System.out.println("Created Records table");
            } if (!DBRequests.salesTableExists()) {
                String createTablesSQL = "CREATE TABLE " + SALES_TABLE_NAME + " (" + RECORD_NUMBER_COLUMN_NAME + " int NOT NULL, " + CONSIGNOR_NUMBER_COLUMN_NAME + " int, " + ARTIST_COLUMN_NAME + " varchar(30), " + TITLE_COLUMN_NAME + " varchar(30), " + SALE_PRICE_COLUMN_NAME + " double, " + STORE_CUT_COLUMN_NAME + " double, " + SALE_DATE_COLUMN_NAME + " varchar(30), PRIMARY KEY (" + RECORD_NUMBER_COLUMN_NAME + "))";
                statement.executeUpdate(createTablesSQL);
                System.out.println("Created Sales table");
            }
            return true;
        } catch (SQLException se) {
            System.out.println("There was a problem with the database and/or tables. Printing stack trace and quitting program.");
            se.printStackTrace();
            return false;
        }
    }

    // Updates the total number of days in store for each record
    static void updateDaysInStore() throws SQLException {
        conn = DBUtils.getConnection();
        statement = conn.createStatement();
        String getDatesReceivedSQL = "SELECT " + DATE_RECEIVED_COLUMN_NAME + ", " + RECORD_NUMBER_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME;
        ResultSet rs = statement.executeQuery(getDatesReceivedSQL);
        LocalDate today = LocalDate.now();
        while (rs.next()) {
            LocalDate date1 = LocalDate.parse(rs.getString(DATE_RECEIVED_COLUMN_NAME));
            int days = Days.daysBetween(date1, today).getDays();
            String updateDaysInStoreSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + DAYS_IN_STORE_COLUMN_NAME + "=" + days + " WHERE " + RECORD_NUMBER_COLUMN_NAME + "=" + rs.getString(RECORD_NUMBER_COLUMN_NAME);
            statement = conn.createStatement();
            statement.executeUpdate(updateDaysInStoreSQL);
        }
    }

    // If any records need to be moved to the basement or donated, the user is alerted
    static void alertDaysInStore() throws SQLException {
        conn = DBUtils.getConnection();
        statement = conn.createStatement();
        String findDaysBasementSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + DAYS_IN_STORE_COLUMN_NAME + " >= " + DAYS_TO_MOVE_TO_BARGAIN_BASEMENT;
        ResultSet rsBasement1 = statement.executeQuery(findDaysBasementSQL);
        while (rsBasement1.next()) {
            if (rsBasement1.getString(LOCATION_COLUMN_NAME)!="Basement") {
                overBargainBasementDays.add(rsBasement1.getInt(RECORD_NUMBER_COLUMN_NAME));
                bargainBasementDetails.add("\nConsignorNum: " + rsBasement1.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + " Record: " + rsBasement1.getString(ARTIST_COLUMN_NAME) + ", " + rsBasement1.getString(TITLE_COLUMN_NAME) + " located at " + rsBasement1.getString(LOCATION_COLUMN_NAME));
            }
        }
        for (int x = 0; x < overBargainBasementDays.size(); x++){
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
        // TODO ask the user if they want the computer to write consignors to call and records to donate to a list and/or delete record from database for them
        if (!overDonateDays.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Attention! These records have been in the system for at least " + DAYS_TO_DONATE + " days, it is time to contact the consignors and donate them:\nRecord Num(s): " + overDonateDays.toString() + "\nRecord Details:" + donateDetails);
        }
        for (int x = 0; x < overBargainBasementDays.size(); x++) {
            String updateLocationSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + LOCATION_COLUMN_NAME + " = 'Basement' WHERE " + RECORD_NUMBER_COLUMN_NAME + " = " + overBargainBasementDays.get(x);
            statement.executeUpdate(updateLocationSQL);
        }
    }

    // Checks if there are too many of any album and alerts the user
    static void checkNumCopiesMax() throws SQLException {
        conn = DBUtils.getConnection();
        statement = conn.createStatement();
        String getAllRecordsSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " ORDER BY " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME;
        ResultSet allRecords = statement.executeQuery(getAllRecordsSQL);
        while (allRecords.next()) {
            String artist = allRecords.getString(ARTIST_COLUMN_NAME);
            String title = allRecords.getString(TITLE_COLUMN_NAME);
            String getAllInfoSQL = "SELECT " + LOCATION_COLUMN_NAME + ", COUNT(" + RECORD_NUMBER_COLUMN_NAME + ") FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + " = '" + artist + "' AND " + TITLE_COLUMN_NAME + " = '" + title + "'";
            statement = conn.createStatement();
            ResultSet rs = statement.executeQuery(getAllInfoSQL);
            rs.next();
            int numCopies = rs.getInt("COUNT(" + RECORD_NUMBER_COLUMN_NAME + ")");
            if (numCopies >= MAX_NUM_COPIES_OF_RECORD){
                overNumCopies.add("\n" + artist + ", " + title + ", located at " + rs.getString(LOCATION_COLUMN_NAME));
                break;
            }
        }
        if (!overNumCopies.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Attention, you have the maximum number of records for the following titles: \n" + overNumCopies);
            overNumCopies.clear();
        }
    }

    // Populates the consignors list from the database
    static void populateConsignorList() throws SQLException {
        conn = DBUtils.getConnection();
        statement = conn.createStatement();
        String populateConsignorsSQL = "SELECT " + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " ORDER BY " + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME;
        PreparedStatement populateConsignorList = conn.prepareStatement(populateConsignorsSQL);
        ResultSet rs = populateConsignorList.executeQuery();
        while (rs.next()) {
            String lastName = rs.getString(LAST_NAME_COLUMN_NAME);
            String firstName = rs.getString(FIRST_NAME_COLUMN_NAME);
            consignorModel.addElement(lastName + ", " + firstName);
        }
    }

    // Populates the records list from the database
    static void populateRecordList() throws SQLException {
        conn = DBUtils.getConnection();
        statement = conn.createStatement();
        String populateRecordsSQL = "SELECT " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " ORDER BY " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME;
        ResultSet rs = statement.executeQuery(populateRecordsSQL);
        while (rs.next()) {
            String artist = rs.getString(ARTIST_COLUMN_NAME);
            String title = rs.getString(TITLE_COLUMN_NAME);
            if (!recordModel.contains(artist + ", " + title)) {
                recordModel.addElement(artist + ", " + title);
            }
        }
    }

    // Populates the JComboBox with ConsignorNums from database
    static void updateComboBox() throws SQLException {
        addConsignorNumComboBoxModel.removeAllElements();
        removeConsignorNumComboBoxModel.removeAllElements();
        payConsignorNumComboBoxModel.removeAllElements();
        String getAllConsignorNumsSQL = "SELECT " + CONSIGNOR_NUMBER_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME;
        ResultSet allConsignorNums = statement.executeQuery(getAllConsignorNumsSQL);
        while (allConsignorNums.next()) {
            addConsignorNumComboBoxModel.addElement(allConsignorNums.getInt(CONSIGNOR_NUMBER_COLUMN_NAME));
            removeConsignorNumComboBoxModel.addElement(allConsignorNums.getInt(CONSIGNOR_NUMBER_COLUMN_NAME));
            payConsignorNumComboBoxModel.addElement(allConsignorNums.getInt(CONSIGNOR_NUMBER_COLUMN_NAME));
        }
    }
}