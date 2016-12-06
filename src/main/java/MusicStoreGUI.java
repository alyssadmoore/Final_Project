import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalDate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;

public class MusicStoreGUI extends JFrame{
    private JPanel rootPanel;
    private JTextField addConsignorFirstName;
    private JTextField addConsignorLastName;
    private JTextField addConsignorPhone;
    private JButton addConsignorButton;
    private JTextField addRecordArtist;
    private JTextField addRecordTitle;
    private JTextField addRecordConsignorNumber;
    private JTextField addRecordPrice;
    private JButton addRecordButton;
    private JTextField removeConsignorNum;
    private JButton removeConsignorButton;
    private JTextField removeRecordNum;
    private JTextField removeRecordPrice;
    private JButton removeRecordButton;
    private JList consignorsList;
    private JList recordsList;
    private JTextField addRecordLocation;
    private JButton quitButton;

    static Statement statement = null;
    static Connection conn = null;

    final String CONSIGNOR_TABLE_NAME = "Consignors";
    final String CONSIGNOR_NUMBER_COLUMN_NAME = "ConsignorNum";
    final String LAST_NAME_COLUMN_NAME = "LastName";
    final String FIRST_NAME_COLUMN_NAME = "FirstName";
    final String PHONE_COLUMN_NAME = "Phone";
    final String RECORDS_TABLE_NAME = "Records";
    final String RECORD_NUMBER_COLUMN_NAME = "RecordNum";
    final String PROFIT_COLUMN_NAME = "Profit";
    final String ARTIST_COLUMN_NAME = "Artist";
    final String TITLE_COLUMN_NAME = "Title";
    final String PRICE_COLUMN_NAME = "Price";
    final String DATE_RECEIVED_COLUMN_NAME = "DateReceived";
    final String DAYS_IN_STORE_COLUMN_NAME = "DaysInStore";
    final String LOCATION_COLUMN_NAME = "Location";
    final String SALES_TABLE_NAME = "Sales";
    final String SALE_PRICE_COLUMN_NAME = "SalePrice";
    final String SALE_DATE_COLUMN_NAME = "SaleDate";
    final int DAYS_TO_MOVE_TO_BARGAIN_BASEMENT = 30;
    final int DAYS_TO_DONATE = 365;
    final double BARGAIN_BASEMENT_PRICE = 1.00;
    final double CONSIGNOR_CUT = 0.4;
    // TODO total store earnings

    // Arraylists hold record numbers only
    ArrayList<Integer> overBargainBasementDays = new ArrayList();
    ArrayList<Integer> overDonateDays = new ArrayList();

    DefaultListModel<String> consignorModel = new DefaultListModel<>();
    DefaultListModel<String> recordModel = new DefaultListModel<>();

    String lookupViewAllSQL = "SELECT * FROM ? WHERE ? = ?";
    String populateListSQL = "SELECT ?, ? FROM ?";
    String selectSQL = "SELECT ? FROM ? WHERE ? = ?";
    String deleteSQL = "DELETE FROM ? WHERE ? = ?";
    String lookupSQL = "SELECT ?, ? FROM ? WHERE ? = ?";
    String updateSQL = "UPDATE ? SET ? = ? WHERE ? = ?";
    String findDaysSQL = "SELECT * FROM ? WHERE ? >= ?";

    MusicStoreGUI() {
        super("Record Store Manager");
        setContentPane(rootPanel);
        consignorsList.setModel(consignorModel);
        recordsList.setModel(recordModel);
        try {
            populateConsignorList();
            populateRecordList();
            updateDaysInStore();
            alertDaysInStore();
        } catch (SQLException sqle) {
            System.out.println("There was a problem setting up. Printing stack trace.");
            sqle.printStackTrace();
        }
        pack();
        createListeners();
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    void createListeners() {
        DBUtils.getDriver();
        conn = DBUtils.getConnection();
        try {statement = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);}
        catch (SQLException sqle) {
            System.exit(1);
        }

        addConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (addConsignorLastName.getText().length()==0 || addConsignorFirstName.getText().length()==0) {
                        JOptionPane.showMessageDialog(null, "Please enter both a first and last name to add a new consignor.");
                    } else {
                        String addConsignorSQL = "INSERT INTO ? (?, ?, ?, ?) VALUES (?, ?, ?, ?)";
                        PreparedStatement addConsignor = conn.prepareStatement(addConsignorSQL);
                        addConsignor.setString(1, RECORDS_TABLE_NAME);
                        addConsignor.setString(2, LAST_NAME_COLUMN_NAME);
                        addConsignor.setString(3, FIRST_NAME_COLUMN_NAME);
                        addConsignor.setString(4, PHONE_COLUMN_NAME);
                        addConsignor.setString(5, PROFIT_COLUMN_NAME);
                        addConsignor.setString(6, addConsignorLastName.getText());
                        addConsignor.setString(7, addConsignorFirstName.getText());
                        addConsignor.setString(8, addConsignorPhone.getText());
                        addConsignor.setDouble(9, 0);
                        addConsignor.executeUpdate();
                        consignorModel.addElement(addConsignorLastName.getText() + ", " + addConsignorFirstName.getText());
                    }
                } catch (SQLException sqle) {
                    System.out.println("There was an error adding a consignor. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        //TODO ask user if the record was sold today or another day, change SQL/PreparedStatement accordingly
        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (entryInt(addRecordConsignorNumber.getText()) && consignorExists(addRecordConsignorNumber.getText())) {
                        if (entryDouble(addRecordPrice.getText())) {
                            if (addRecordArtist.getText().length()==0 || addRecordTitle.getText().length()==0) {
                                JOptionPane.showMessageDialog(null, "Please enter both an artist and a title to add a new record.");
                            } else {
                                String addRecordSQL = "INSERT INTO ? (?, ?, ?, ?, ?, ?, ?) VALUES (?, ?, ?, ?, ?, ?, ?)";
                                PreparedStatement addRecord = conn.prepareStatement(addRecordSQL);
                                addRecord.setString(1, RECORDS_TABLE_NAME);
                                addRecord.setString(2, CONSIGNOR_NUMBER_COLUMN_NAME);
                                addRecord.setString(3, ARTIST_COLUMN_NAME);
                                addRecord.setString(4, TITLE_COLUMN_NAME);
                                addRecord.setString(5, PRICE_COLUMN_NAME);
                                addRecord.setString(6, DATE_RECEIVED_COLUMN_NAME);
                                addRecord.setString(7, DAYS_IN_STORE_COLUMN_NAME);
                                addRecord.setString(8, LOCATION_COLUMN_NAME);
                                addRecord.setInt(9, Integer.parseInt(addRecordConsignorNumber.getText()));
                                addRecord.setString(10, addRecordArtist.getText());
                                addRecord.setString(11, addRecordTitle.getText());
                                addRecord.setDouble(12, Double.parseDouble(addRecordPrice.getText()));
                                addRecord.setString(13, LocalDate.now().toString());
                                addRecord.setInt(14, 0);
                                addRecord.setString(15, addRecordLocation.getText());
                                addRecord.executeUpdate();
                                if (!recordModel.contains(addRecordArtist.getText() + ", " + addRecordTitle.getText())) {
                                    recordModel.addElement(addRecordArtist.getText() + ", " + addRecordTitle.getText());
                                }
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "You must enter price with a period to differentiate dollars/cents and no dollar sign, like this: 9.99");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Either you didn't enter an integer (1, 2, 3, etc.) or that consignor does not exist. Please try again.");
                    }
                } catch (SQLException sqle) {
                    System.out.println("There was an error adding a record. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        removeConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (entryInt(removeConsignorNum.getText()) && consignorExists(removeConsignorNum.getText())) {
                        int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Consignor " + removeConsignorNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                        if (check == JOptionPane.YES_OPTION) {
                            PreparedStatement deleteConsignor = conn.prepareStatement(deleteSQL);
                            deleteConsignor.setString(1, CONSIGNOR_TABLE_NAME);
                            deleteConsignor.setString(2, CONSIGNOR_NUMBER_COLUMN_NAME);
                            deleteConsignor.setInt(3, Integer.parseInt(removeConsignorNum.getText()));
                            String fullname = findConsignorGivenNum(removeConsignorNum.getText());
                            deleteConsignor.executeUpdate();
                            consignorModel.removeElement(fullname);
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Could not find Consignor. Did you enter an integer (1, 2, 3, etc.)? ");
                    }
                } catch (SQLException sqle) {
                    System.out.println("There was an error deleting a consignor. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        removeRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (entryInt(removeRecordNum.getText()) && recordExists(removeRecordNum.getText())) {
                        int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Record " + removeRecordNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                        if (check == JOptionPane.YES_OPTION) {

                            // Getting the consignor number attached to the record sold
                            PreparedStatement getCurrentConsignor = conn.prepareStatement(selectSQL);
                            getCurrentConsignor.setString(1, CONSIGNOR_NUMBER_COLUMN_NAME);
                            getCurrentConsignor.setString(2, RECORDS_TABLE_NAME);
                            getCurrentConsignor.setString(3, RECORD_NUMBER_COLUMN_NAME);
                            getCurrentConsignor.setInt(4, Integer.parseInt(removeRecordNum.getText()));
                            ResultSet currentConsignorRS = getCurrentConsignor.executeQuery();
                            currentConsignorRS.next();
                            String currentConsignorNum = currentConsignorRS.getString(CONSIGNOR_NUMBER_COLUMN_NAME);

                            // Getting the current profit of the consignor attached to the record sold
                            PreparedStatement getCurrentProfit = conn.prepareStatement(selectSQL);
                            getCurrentProfit.setString(1, PROFIT_COLUMN_NAME);
                            getCurrentProfit.setString(2, CONSIGNOR_TABLE_NAME);
                            getCurrentProfit.setString(3, CONSIGNOR_NUMBER_COLUMN_NAME);
                            getCurrentProfit.setString(4, currentConsignorNum);
                            ResultSet consignorCurrentProfitRS = getCurrentProfit.executeQuery();
                            consignorCurrentProfitRS.next();

                            // Calculating consignor's cut, new profit and store's cut
                            double consignorCurrentProfit = consignorCurrentProfitRS.getDouble(PROFIT_COLUMN_NAME);
                            double consignorCut = Double.parseDouble(removeRecordPrice.getText()) * CONSIGNOR_CUT;
                            //TODO do something with storecut
                            double storeCut = Double.parseDouble(removeRecordPrice.getText()) - consignorCut;
                            consignorCurrentProfit += consignorCut;

                            // Updating consignor's profit
                            PreparedStatement updateConsignorProfit = conn.prepareStatement(updateSQL);
                            updateConsignorProfit.setString(1, CONSIGNOR_TABLE_NAME);
                            updateConsignorProfit.setString(2, PROFIT_COLUMN_NAME);
                            updateConsignorProfit.setDouble(3, consignorCurrentProfit);
                            updateConsignorProfit.setString(4, CONSIGNOR_NUMBER_COLUMN_NAME);
                            updateConsignorProfit.setString(5, currentConsignorNum);
                            updateConsignorProfit.executeUpdate();

                            // Finding the full record name (artist & title) and deleting it from the database
                            String fullAlbum = findRecordGivenNum(removeRecordNum.getText());
                            PreparedStatement deleteRecord = conn.prepareStatement(deleteSQL);
                            deleteRecord.setString(1, RECORDS_TABLE_NAME);
                            deleteRecord.setString(2, RECORD_NUMBER_COLUMN_NAME);
                            deleteRecord.setInt(3, Integer.parseInt(removeRecordNum.getText()));
                            deleteRecord.executeUpdate();

                            // Extracting the title and artist of the record sold, searching database to see if there are still copies left in the store, if so don't delete the record from the JList & vice versa
                            String title = fullAlbum.substring(fullAlbum.indexOf(",")+2);
                            String artist = fullAlbum.substring(0, fullAlbum.indexOf(","));
                            String findMoreRecordsSQL = "SELECT ? , ? FROM ? WHERE ? = ? AND ? = ?";
                            PreparedStatement findMoreRecords = conn.prepareStatement(findMoreRecordsSQL);
                            findMoreRecords.setString(1, ARTIST_COLUMN_NAME);
                            findMoreRecords.setString(2, RECORDS_TABLE_NAME);
                            findMoreRecords.setString(3, ARTIST_COLUMN_NAME);
                            findMoreRecords.setString(4, TITLE_COLUMN_NAME);
                            findMoreRecords.setString(5, artist);
                            findMoreRecords.setString(6, TITLE_COLUMN_NAME);
                            findMoreRecords.setString(7, title);
                            ResultSet rs = findMoreRecords.executeQuery();
                            if (!rs.next()) {
                                recordModel.removeElement(fullAlbum);
                            }

                            //TODO ask user if sale was today or another day, change SQL accordingly
                            // Adding the information about the record sold to the Sales table
                            String insertSalesSQL = "INSERT INTO " + SALES_TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?)";
                            PreparedStatement insertSales = conn.prepareStatement(insertSalesSQL);
                            insertSales.setInt(1, Integer.parseInt(removeRecordNum.getText()));
                            insertSales.setInt(2, Integer.parseInt(currentConsignorNum));
                            insertSales.setString(3 , artist);
                            insertSales.setString(4, title);
                            insertSales.setDouble(5, Double.parseDouble(removeRecordPrice.getText()));
                            insertSales.setString(6, DateTime.now().toString());
                            insertSales.executeUpdate();
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "It seems you've entered a non-integer value. Please enter an integer (1, 2, 3, etc.) and try again.");
                    }
                } catch (SQLException sqle) {
                    JOptionPane.showMessageDialog(null, "There was an error deleting a record. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        consignorsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    String fullName = consignorsList.getSelectedValue().toString();
                    String first = fullName.substring(fullName.indexOf(",")+2);
                    String last = fullName.substring(0, fullName.indexOf(","));
                    String selectAllTwoVarSQL = "SELECT * FROM ? WHERE ? = ? AND ? = ?" ;
                    PreparedStatement selectAllTwoVar = conn.prepareStatement(selectAllTwoVarSQL);
                    selectAllTwoVar.setString(1, CONSIGNOR_TABLE_NAME);
                    selectAllTwoVar.setString(2, LAST_NAME_COLUMN_NAME);
                    selectAllTwoVar.setString(3, last);
                    selectAllTwoVar.setString(4, FIRST_NAME_COLUMN_NAME);
                    selectAllTwoVar.setString(5, first);
                    ResultSet rs = selectAllTwoVar.executeQuery();
                    rs.next();
                    // TODO add records in store, avg earnings per album, etc. metadata
                    JOptionPane.showMessageDialog(null, " ID Number: " + rs.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + "\n Phone number: " + rs.getString(PHONE_COLUMN_NAME) + "\n Total profit: $" +
                            rs.getString(PROFIT_COLUMN_NAME), rs.getString(LAST_NAME_COLUMN_NAME) + ", " + rs.getString(FIRST_NAME_COLUMN_NAME), JOptionPane.PLAIN_MESSAGE);
                } catch (SQLException sqle) {
                    JOptionPane.showMessageDialog(null, "Sorry, there was an error retrieving information. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        recordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    String fullAlbum = recordsList.getSelectedValue().toString();
                    String title = fullAlbum.substring(fullAlbum.indexOf(",")+2);
                    String artist = fullAlbum.substring(0, fullAlbum.indexOf(","));
                    String getAllInfoSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + "='" + artist + "' AND " + TITLE_COLUMN_NAME + "='" + title + "'";
                    ResultSet rs = statement.executeQuery(getAllInfoSQL);
                    rs.last();
                    int numRows = rs.getRow();
                    rs.beforeFirst();
                    while (rs.next()) {
                        // TODO add average length of time in store, average sale price, etc. metadata
                        JOptionPane.showMessageDialog(null, " Duplicates of this record: " + numRows + "\n ID Number: " + rs.getString(RECORD_NUMBER_COLUMN_NAME) + "\n Consignor Number: " +
                                rs.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + "\n Price: " + rs.getString(PRICE_COLUMN_NAME) + "\n Date received: " + rs.getString(DATE_RECEIVED_COLUMN_NAME) +
                                "\n Days in store: " + rs.getString(DAYS_IN_STORE_COLUMN_NAME) + "\n Location: " + rs.getString(LOCATION_COLUMN_NAME) + "\n", rs.getString(TITLE_COLUMN_NAME) +
                                " by " + rs.getString(ARTIST_COLUMN_NAME), JOptionPane.PLAIN_MESSAGE);
                    }
                } catch (SQLException sqle) {
                    JOptionPane.showMessageDialog(null, "Sorry, there was an error retrieving information. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                overDonateDays.clear();
                overBargainBasementDays.clear();
                try {
                    conn.close();
                    statement.close();
                    System.exit(0);
                } catch (SQLException sqle) {
                    JOptionPane.showMessageDialog(null, "There was an error closing resources. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });
    }

    // Updates the total number of days in store for each record on startup
    void updateDaysInStore() throws SQLException{
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

    // If any records need to be moved to the basement or donated, the user is alerted on startup
    void alertDaysInStore() throws SQLException {
        conn = DBUtils.getConnection();
        PreparedStatement findDaysBasement = conn.prepareStatement(findDaysSQL);
        findDaysBasement.setString(1, RECORDS_TABLE_NAME);
        findDaysBasement.setString(2, DAYS_IN_STORE_COLUMN_NAME);
        findDaysBasement.setInt(3, DAYS_TO_MOVE_TO_BARGAIN_BASEMENT);
        ResultSet rsBasement = findDaysBasement.executeQuery();
        while (rsBasement.next()) {
            if (rsBasement.getString(LOCATION_COLUMN_NAME)!="Basement") {
                overBargainBasementDays.add(rsBasement.getInt(RECORD_NUMBER_COLUMN_NAME));
                PreparedStatement updatePrice = conn.prepareStatement(updateSQL);
                updatePrice.setString(1, RECORDS_TABLE_NAME);
                updatePrice.setString(2, PRICE_COLUMN_NAME);
                updatePrice.setDouble(3, BARGAIN_BASEMENT_PRICE);
                updatePrice.setString(4, RECORD_NUMBER_COLUMN_NAME);
                updatePrice.setInt(5, rsBasement.getInt(RECORD_NUMBER_COLUMN_NAME));
                updatePrice.executeUpdate();

                PreparedStatement updateLocation = conn.prepareStatement(updateSQL);
                updateLocation.setString(1, RECORDS_TABLE_NAME);
                updateLocation.setString(2, LOCATION_COLUMN_NAME);
                updateLocation.setString(3, "Basement");
                updateLocation.setString(4, RECORD_NUMBER_COLUMN_NAME);
                updateLocation.setInt(5, rsBasement.getInt(RECORD_NUMBER_COLUMN_NAME));
                updateLocation.executeUpdate();
            }
        }
        //TODO add artist, title and location for each entry to be moved to basement
        if (!overBargainBasementDays.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Attention! These records have been in the system for " + DAYS_TO_MOVE_TO_BARGAIN_BASEMENT + " days, it is time to move them to the bagain basement and change price to $" + BARGAIN_BASEMENT_PRICE + ":\nRecordNum(s): " + overBargainBasementDays.toString());
        }

        PreparedStatement findDaysDonate = conn.prepareStatement(findDaysSQL);
        findDaysDonate.setString(1, RECORDS_TABLE_NAME);
        findDaysDonate.setString(2, DAYS_IN_STORE_COLUMN_NAME);
        findDaysDonate.setInt(3, DAYS_TO_DONATE);
        ResultSet rsDonate = findDaysDonate.executeQuery();
        while (rsDonate.next()) {
            overDonateDays.add(rsDonate.getInt(RECORD_NUMBER_COLUMN_NAME));
        }
        // TODO ask the user if they want the computer to write consignors to call and records to donate to a list and/or delete record from database for them
        if (!overDonateDays.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Attention! These records have been in the system for at least " + DAYS_TO_DONATE + " days, it is time to donate them:\nRecord Num(s): " + overDonateDays.toString());
        }
    }

    // Given user entry, returns a consignor's first and last name
    String findConsignorGivenNum(String number) throws SQLException{
        conn = DBUtils.getConnection();
        PreparedStatement findConsignor = conn.prepareStatement(lookupSQL);
        findConsignor.setString(1,LAST_NAME_COLUMN_NAME);
        findConsignor.setString(2, FIRST_NAME_COLUMN_NAME);
        findConsignor.setString(3, CONSIGNOR_TABLE_NAME);
        findConsignor.setString(4, CONSIGNOR_NUMBER_COLUMN_NAME);
        findConsignor.setInt(5, Integer.parseInt(number));
        ResultSet rs = findConsignor.executeQuery();
        rs.next();
        String lastname = rs.getString(LAST_NAME_COLUMN_NAME);
        String firstname = rs.getString(FIRST_NAME_COLUMN_NAME);
        return (lastname + ", " + firstname);
    }

    // Given user entry, returns a record's artist and title
    String findRecordGivenNum(String number) throws SQLException{
        conn = DBUtils.getConnection();
        PreparedStatement findRecord = conn.prepareStatement(lookupSQL);
        findRecord.setString(1, ARTIST_COLUMN_NAME);
        findRecord.setString(2, TITLE_COLUMN_NAME);
        findRecord.setString(3, RECORDS_TABLE_NAME);
        findRecord.setString(4, RECORD_NUMBER_COLUMN_NAME);
        findRecord.setInt(5, Integer.parseInt(number));
        ResultSet rs = findRecord.executeQuery();
        rs.next();
        String artist = rs.getString(ARTIST_COLUMN_NAME);
        String title = rs.getString(TITLE_COLUMN_NAME);
        return (artist + ", " + title);
    }

    // Populates the consignors list on startup from the database
    void populateConsignorList() throws SQLException {
        conn = DBUtils.getConnection();
        PreparedStatement populateList = conn.prepareStatement(populateListSQL);
        populateList.setString(1, LAST_NAME_COLUMN_NAME);
        populateList.setString(2, FIRST_NAME_COLUMN_NAME);
        populateList.setString(3, CONSIGNOR_NUMBER_COLUMN_NAME);
        ResultSet rs = populateList.executeQuery();
        while (rs.next()) {
            String lastName = rs.getString(LAST_NAME_COLUMN_NAME);
            String firstName = rs.getString(FIRST_NAME_COLUMN_NAME);
            consignorModel.addElement(lastName + ", " + firstName);
        }
    }

    // Populates the records list on startup from the database
    void populateRecordList() throws SQLException {
        conn = DBUtils.getConnection();
        PreparedStatement populateRecordList = conn.prepareStatement(populateListSQL);
        populateRecordList.setString(1, ARTIST_COLUMN_NAME);
        populateRecordList.setString(2, TITLE_COLUMN_NAME);
        populateRecordList.setString(3, RECORDS_TABLE_NAME);
        ResultSet rs = populateRecordList.executeQuery();
        while (rs.next()) {
            String artist = rs.getString(ARTIST_COLUMN_NAME);
            String title = rs.getString(TITLE_COLUMN_NAME);
            if (!recordModel.contains(artist + ", " + title)) {
                recordModel.addElement(artist + ", " + title);
            }
        }
    }

    // Checks if a consignor exists based on user entry
    boolean consignorExists(String userEntry) throws SQLException{
        conn = DBUtils.getConnection();
        PreparedStatement checkConsignorPresent = conn.prepareStatement(lookupViewAllSQL);
        checkConsignorPresent.setString(1, CONSIGNOR_TABLE_NAME);
        checkConsignorPresent.setString(2, CONSIGNOR_NUMBER_COLUMN_NAME);
        checkConsignorPresent.setInt(3, Integer.parseInt(userEntry));
        ResultSet consignorsRS = checkConsignorPresent.executeQuery();
        return consignorsRS.next();
    }

    // Checks if a record exists based on user entry
    boolean recordExists(String userEntry) throws SQLException {
        conn = DBUtils.getConnection();
        PreparedStatement checkRecordPresent = conn.prepareStatement(lookupViewAllSQL);
        checkRecordPresent.setString(1, RECORDS_TABLE_NAME);
        checkRecordPresent.setString(2, RECORD_NUMBER_COLUMN_NAME);
        checkRecordPresent.setInt(3, Integer.parseInt(userEntry));
        ResultSet recordsRS = checkRecordPresent.executeQuery();
        return recordsRS.next();
    }

    // Checks if user entry is an integer
    boolean entryInt(String entryValue) {
        try {
            Integer.parseInt(entryValue);}
        catch (NumberFormatException nfe) {return false;}
        catch (NullPointerException npe) {return false;}
        return true;
    }

    // Checks if user entry is a double
    boolean entryDouble(String entryValue) {
        try {
            Double.parseDouble(entryValue);}
        catch (NumberFormatException nfe) {return false;}
        catch (NullPointerException npe) {return false;}
        return true;
    }
}