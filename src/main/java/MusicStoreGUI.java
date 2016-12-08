import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;

public class MusicStoreGUI extends JFrame{
    JPanel rootPanel;
    JTextField addConsignorFirstName;
    JTextField addConsignorLastName;
    JTextField addConsignorPhone;
    JButton addConsignorButton;
    JTextField addRecordArtist;
    JTextField addRecordTitle;
    JTextField addRecordConsignorNumber;
    JTextField addRecordPrice;
    JButton addRecordButton;
    JButton removeConsignorButton;
    JTextField removeRecordNum;
    JTextField removeRecordPrice;
    JButton removeRecordButton;
    JList consignorsList;
    JList recordsList;
    JTextField addRecordLocation;
    JButton quitButton;
    private JComboBox consignorNumComboBox;
    private JTabbedPane tabbedLists;
    private JPanel Consignors;
    private JPanel Records;
    private JTabbedPane AddTab;
    private JPanel AddConsignor;
    private JTabbedPane RemoveTab;
    private JButton searchButton;
    private JButton statisticsButton;
    private JComboBox payAConsignorComboBox;
    private JTextField payAConsignorAmount;
    private JButton payAConsignorButton;
    private JPanel AddRecord;
    private JComboBox addAConsignorComboBox;

    static Statement statement = null;
    static Connection conn = null;

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

    // 40% = 0.4
    final double CONSIGNOR_CUT = 0.4;
    final static int MAX_NUM_COPIES_OF_RECORD = 50;

    // TODO total store earnings

    // overBargainBasementDays and overDonateDays hold record numbers only
    static ArrayList<Integer> overBargainBasementDays = new ArrayList();
    static ArrayList<String> bargainBasementDetails = new ArrayList<>();
    static ArrayList<Integer> overDonateDays = new ArrayList();
    static ArrayList<String> donateDetails = new ArrayList<>();
    ArrayList<String> associatedRecords = new ArrayList();

    static DefaultListModel<String> consignorModel = new DefaultListModel<>();
    static DefaultListModel<String> recordModel = new DefaultListModel<>();
    static DefaultComboBoxModel<Integer> addConsignorNumComboBoxModel = new DefaultComboBoxModel();
    static DefaultComboBoxModel<Integer> payConsignorNumComboBoxModel = new DefaultComboBoxModel();
    static DefaultComboBoxModel<Integer> removeConsignorNumComboBoxModel = new DefaultComboBoxModel();

    MusicStoreGUI() {
        super("Record Store Manager");
        setContentPane(rootPanel);
        consignorsList.setModel(consignorModel);
        recordsList.setModel(recordModel);
        consignorNumComboBox.setModel(removeConsignorNumComboBoxModel);
        payAConsignorComboBox.setModel(payConsignorNumComboBoxModel);
        addAConsignorComboBox.setModel(addConsignorNumComboBoxModel);
        tabbedLists.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        tabbedLists.setTitleAt(1, RECORDS_TABLE_NAME);
        AddTab.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        AddTab.setTitleAt(1, RECORDS_TABLE_NAME);
        RemoveTab.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        RemoveTab.setTitleAt(1, RECORDS_TABLE_NAME);
        try {
            Startup.populateConsignorList();
            Startup.populateRecordList();
            Startup.updateDaysInStore();
            Startup.alertDaysInStore();
            Startup.checkNumCopiesMax();
            Startup.updateComboBox();
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
                    DBUtils.getDriver();
                    Connection conn = DBUtils.getConnection();
                    if (addConsignorLastName.getText().trim().length() == 0 || addConsignorFirstName.getText().trim().length() == 0 || addConsignorPhone.getText().trim().length() == 0) {
                        JOptionPane.showMessageDialog(null, "Please enter a first name, last name, and phone number to add a new consignor.");
                    } else {
                        String addConsignorSQL = "INSERT INTO " + CONSIGNOR_TABLE_NAME + "(" + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + ", " + PHONE_COLUMN_NAME + ", " + AMOUNT_OWED_COLUMN_NAME + ", " + PROFIT_COLUMN_NAME + ") VALUES (?, ?, ?, ?, ?)";
                        PreparedStatement addConsignor = conn.prepareStatement(addConsignorSQL);
                        addConsignor.setString(1, addConsignorLastName.getText());
                        addConsignor.setString(2, addConsignorFirstName.getText());
                        addConsignor.setString(3, addConsignorPhone.getText());
                        addConsignor.setDouble(4, 0.0);
                        addConsignor.setDouble(5, 0.0);
                        addConsignor.executeUpdate();
                        consignorModel.addElement(addConsignorLastName.getText() + ", " + addConsignorFirstName.getText());
                        Startup.updateComboBox();
                        JOptionPane.showMessageDialog(null, "Consignor added successfully");
                    }
                } catch (SQLException sqle) {
                    System.out.println("There was an error adding a consignor. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (UserEntryValidation.entryDouble(addRecordPrice.getText())) {
                        if (addRecordArtist.getText().trim().length() == 0 || addRecordTitle.getText().trim().length() == 0) {
                            JOptionPane.showMessageDialog(null, "Please enter both an artist and a title to add a new record.");
                        } else {
                            String addRecordSQL = "INSERT INTO " + RECORDS_TABLE_NAME + " (" + CONSIGNOR_NUMBER_COLUMN_NAME + ", " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + ", " + PRICE_COLUMN_NAME + ", " + DATE_RECEIVED_COLUMN_NAME + ", " + DAYS_IN_STORE_COLUMN_NAME + ", " + LOCATION_COLUMN_NAME + ") VALUES (?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement addRecord = conn.prepareStatement(addRecordSQL);
                            addRecord.setInt(1, (Integer)addAConsignorComboBox.getSelectedItem());
                            addRecord.setString(2, addRecordArtist.getText());
                            addRecord.setString(3, addRecordTitle.getText());
                            addRecord.setDouble(4, Double.parseDouble(addRecordPrice.getText()));
                            addRecord.setString(5, LocalDate.now().toString());
                            addRecord.setInt(6, 0);
                            addRecord.setString(7, addRecordLocation.getText());
                            addRecord.executeUpdate();
                            JOptionPane.showMessageDialog(null, "Record added successfully");
                            UserEntryDBRequests.checkNumCopies(addRecordArtist.getText(), addRecordTitle.getText());
                            if (!recordModel.contains(addRecordArtist.getText() + ", " + addRecordTitle.getText())) {
                                recordModel.addElement(addRecordArtist.getText() + ", " + addRecordTitle.getText());
                            }
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "You must enter price with a period to differentiate dollars/cents and no dollar sign, like this: 9.99");
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
                    int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Consignor " + (Integer)consignorNumComboBox.getSelectedItem() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                    if (check == JOptionPane.YES_OPTION) {
                        String deleteSQL = "DELETE FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = ?";
                        PreparedStatement deleteConsignor = conn.prepareStatement(deleteSQL);
                        deleteConsignor.setInt(1, (Integer)consignorNumComboBox.getSelectedItem());
                        String fullname = UserEntryDBRequests.findConsignorGivenNum((Integer)consignorNumComboBox.getSelectedItem());
                        deleteConsignor.executeUpdate();
                        consignorModel.removeElement(fullname);
                        Startup.updateComboBox();
                        JOptionPane.showMessageDialog(null, "Consignor deleted successfully");
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
                    if (UserEntryValidation.entryInt(removeRecordNum.getText()) && UserEntryDBRequests.recordExists(removeRecordNum.getText())) {
                        int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Record " + removeRecordNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                        if (check == JOptionPane.YES_OPTION) {

                            // Getting the consignor number attached to the record sold
                            String selectConsignorSQL = "SELECT " + CONSIGNOR_NUMBER_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
                            PreparedStatement getCurrentConsignor = conn.prepareStatement(selectConsignorSQL);
                            getCurrentConsignor.setInt(1, Integer.parseInt(removeRecordNum.getText()));
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
                            double consignorCut = Double.parseDouble(removeRecordPrice.getText()) * CONSIGNOR_CUT;
                            double storeCut = Double.parseDouble(removeRecordPrice.getText()) - consignorCut;
                            consignorCurrentAmountOwed += consignorCut;

                            // Updating consignor's amount owed
                            String updateSQL = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + AMOUNT_OWED_COLUMN_NAME + " = " + consignorCurrentAmountOwed + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + currentConsignorNum;
                            statement.executeUpdate(updateSQL);

                            // Finding the full record name (artist & title) and deleting it from the database
                            String fullAlbum = UserEntryDBRequests.findRecordGivenNum(Integer.parseInt(removeRecordNum.getText()));
                            String deleteSQL = "DELETE FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + " = ?";
                            PreparedStatement deleteRecord = conn.prepareStatement(deleteSQL);
                            deleteRecord.setInt(1, Integer.parseInt(removeRecordNum.getText()));
                            deleteRecord.executeUpdate();

                            // Extracting the title and artist of the record sold, searching database to see if there are still copies left in the store, if so don't delete the record from the JList & vice versa
                            String title = fullAlbum.substring(fullAlbum.indexOf(",")+2);
                            String artist = fullAlbum.substring(0, fullAlbum.indexOf(","));
                            String findMoreRecordsSQL = "SELECT " + ARTIST_COLUMN_NAME + " , " + TITLE_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + " = ? AND " + TITLE_COLUMN_NAME + " = ?";
                            PreparedStatement findMoreRecords = conn.prepareStatement(findMoreRecordsSQL);
                            findMoreRecords.setString(1, artist);
                            findMoreRecords.setString(2, title);
                            ResultSet rs = findMoreRecords.executeQuery();
                            if (!rs.next()) {
                                recordModel.removeElement(fullAlbum);
                            }

                            // Adding the information about the record sold to the Sales table (assumes the record came into the store the same day the record is being created in the DB)
                            String insertSalesSQL = "INSERT INTO " + SALES_TABLE_NAME + " VALUES (?, ?, ?, ?, ?, ?, ?)";
                            PreparedStatement insertSales = conn.prepareStatement(insertSalesSQL);
                            insertSales.setInt(1, Integer.parseInt(removeRecordNum.getText()));
                            insertSales.setInt(2, Integer.parseInt(currentConsignorNum));
                            insertSales.setString(3 , artist);
                            insertSales.setString(4, title);
                            insertSales.setDouble(5, Double.parseDouble(removeRecordPrice.getText()));
                            insertSales.setDouble(6, storeCut);
                            insertSales.setString(7, DateTime.now().toString());
                            insertSales.executeUpdate();

                            JOptionPane.showMessageDialog(null, "Record removed successfully");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Either that record doesn't exist or you entered a non-integer value. Please enter an integer (1, 2, 3, etc.) and try again.");
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
                    String selectAllTwoVarSQL = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + LAST_NAME_COLUMN_NAME + " = '" + last + "' AND " + FIRST_NAME_COLUMN_NAME + " = '" + first + "'";
                    ResultSet rsConsignor = statement.executeQuery(selectAllTwoVarSQL);
                    rsConsignor.next();
                    statement = conn.createStatement();
                    String selectAssociatedRecordsSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + rsConsignor.getString(CONSIGNOR_NUMBER_COLUMN_NAME);
                    ResultSet rsRecord = statement.executeQuery(selectAssociatedRecordsSQL);
                    while (rsRecord.next()) {
                        associatedRecords.add("\nRecordNum: " + rsRecord.getString(RECORD_NUMBER_COLUMN_NAME) + ", Artist: " + rsRecord.getString(ARTIST_COLUMN_NAME) + ", Title: " + rsRecord.getString(TITLE_COLUMN_NAME) + ", Location: " + rsRecord.getString(LOCATION_COLUMN_NAME));
                    }
                    // TODO add etc metadata(?)
                    JOptionPane.showMessageDialog(null, " ID Number: " + rsConsignor.getString(CONSIGNOR_NUMBER_COLUMN_NAME) + "\n Phone number: " + rsConsignor.getString(PHONE_COLUMN_NAME) + "\n Amount Owed: $" + rsConsignor.getString(AMOUNT_OWED_COLUMN_NAME) + "\nTotal profit: $" +
                            rsConsignor.getString(PROFIT_COLUMN_NAME) + "\n\nAssociated records: " + associatedRecords, rsConsignor.getString(LAST_NAME_COLUMN_NAME) + ", " + rsConsignor.getString(FIRST_NAME_COLUMN_NAME), JOptionPane.PLAIN_MESSAGE);
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
                    String getAllInfoSQL = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + ARTIST_COLUMN_NAME + " = '" + artist + "' AND " + TITLE_COLUMN_NAME + " = '" + title + "'";
                    ResultSet rs = statement.executeQuery(getAllInfoSQL);
                    rs.last();
                    int numRows = rs.getRow();
                    rs.beforeFirst();
                    while (rs.next()) {
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

        // TODO metadata(?)
        statisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });

        payAConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (UserEntryValidation.entryDouble(payAConsignorAmount.getText())){
                    Double amountToPay = Double.parseDouble(payAConsignorAmount.getText());
                    try {
                        String getConsignorInfoSQL = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + payAConsignorComboBox.getSelectedItem();
                        ResultSet rsConsignor = statement.executeQuery(getConsignorInfoSQL);
                        rsConsignor.next();
                        Double newAmountOwed = rsConsignor.getDouble(AMOUNT_OWED_COLUMN_NAME) - amountToPay;
                        if (newAmountOwed < 0.0) {
                            JOptionPane.showMessageDialog(null, "Consignor's amount owed is already at zero.");
                        } else {
                            Double newProfit = rsConsignor.getDouble(PROFIT_COLUMN_NAME) + amountToPay;
                            String updateAmountOwed = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + AMOUNT_OWED_COLUMN_NAME + " = " + newAmountOwed + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + payAConsignorComboBox.getSelectedItem();
                            statement.executeUpdate(updateAmountOwed);
                            String updateProfit = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + PROFIT_COLUMN_NAME + " = " + newProfit + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + " = " + payAConsignorComboBox.getSelectedItem();
                            statement.executeUpdate(updateProfit);
                            JOptionPane.showMessageDialog(null, "Amount Owed and Total Profit updated successfully");
                        }
                    } catch (SQLException sqle){
                        JOptionPane.showMessageDialog(null, "There was an error updating the amount owed or total profit, printing stack trace.");
                        sqle.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "It seems you didn't enter the right data type. Enter payment with a period separating dollars and cents with no other symbols, like this: 10.00");
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
}