import javax.swing.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserEntryDBRequests extends MusicStoreGUI{

    // Returns a consignor's first and last name in Jlist format
    static String findConsignorGivenNum(int number) throws SQLException {
        conn = DBUtils.getConnection();
        String lookupConsignorSQL = "SELECT " + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = ?";
        PreparedStatement findConsignor = conn.prepareStatement(lookupConsignorSQL);
        findConsignor.setInt(1, number);
        ResultSet rs = findConsignor.executeQuery();
        rs.next();
        String lastname = rs.getString(LAST_NAME_COLUMN_NAME);
        String firstname = rs.getString(FIRST_NAME_COLUMN_NAME);
        return (lastname + ", " + firstname);
    }

    // Returns a record's artist and title in Jlist format
    static String findRecordGivenNum(int number) throws SQLException{
        conn = DBUtils.getConnection();
        String lookupRecordSQL = "SELECT " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
        PreparedStatement findRecord = conn.prepareStatement(lookupRecordSQL);
        findRecord.setInt(1, number);
        ResultSet rs = findRecord.executeQuery();
        rs.next();
        String artist = rs.getString(ARTIST_COLUMN_NAME);
        String title = rs.getString(TITLE_COLUMN_NAME);
        return (artist + ", " + title);
    }

    // Checks if a certain consignor exists
    static boolean consignorExists(String userEntry) throws SQLException{
        conn = DBUtils.getConnection();
        String lookupViewAllConsignorsSQL = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = ?";
        PreparedStatement checkConsignorPresent = conn.prepareStatement(lookupViewAllConsignorsSQL);
        checkConsignorPresent.setInt(1, Integer.parseInt(userEntry));
        ResultSet consignorsRS = checkConsignorPresent.executeQuery();
        return consignorsRS.next();
    }

    // Checks if a certain record exists
    static boolean recordExists(String userEntry) throws SQLException {
        conn = DBUtils.getConnection();
        String lookupViewAllRecordsSQL = "SELECT * FROM " + RECORDS_TABLE_NAME  + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
        PreparedStatement checkRecordPresent = conn.prepareStatement(lookupViewAllRecordsSQL);
        checkRecordPresent.setInt(1, Integer.parseInt(userEntry));
        ResultSet recordsRS = checkRecordPresent.executeQuery();
        return recordsRS.next();
    }

    // Checks the number of copies of a record in database
    static void checkNumCopies(String artist, String title) throws SQLException{
        conn = DBUtils.getConnection();
        String getAllInfoSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + "= ? AND " + TITLE_COLUMN_NAME + "= ?";
        PreparedStatement ps = conn.prepareStatement(getAllInfoSQL);
        ps.setString(1, artist);
        ps.setString(2, title);
        ResultSet rs = ps.executeQuery();
        rs.last();
        int numRows = rs.getRow();
        if (numRows >= MAX_NUM_COPIES_OF_RECORD){
            JOptionPane.showMessageDialog(null, "Attention: You have the maximum number of albums of this title: " + artist + ", " + title);
        }
    }
}