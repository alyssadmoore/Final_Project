import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    private JTextField removeRecordDate;
    private JButton removeRecordButton;
    private JList consignorsList;
    private JButton add1DayButton;
    private JButton add1WeekButton;
    private JList recordsList;
    private JTextField addRecordLocation;
    private JButton quitButton;

    static Statement statement = null;
    static Connection conn = null;

    final double CONSIGNOR_CUT = 0.4;

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
    final String DAYS_IN_STORE_COLUMN_NAME = "DaysInStore";
    final String LOCATION_COLUMN_NAME = "Location";
    final String SALES_TABLE_NAME = "Sales";
    final String SALE_PRICE_COLUMN_NAME = "SalePrice";
    final String SALE_DATE_COLUMN_NAME = "SaleDate";

    // sales only holds values while the program is open, and writes to the master file when closed so there aren't duplicate entries
    // sales format: line 1: RecordNum, line 2: SaleDate, line 3: SalePrice (every 3 lines equals one entry)
    // Sales table: RecordNum, ConsignorNum(Records), Artist(Records), Title(Records), SalePrice, SaleDate
    ArrayList<String> sales = new ArrayList<>();
    // TODO add metadata (artist, title)

    // TODO total store earnings?

    MusicStoreGUI() {
        setContentPane(rootPanel);
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
                String addConsignorSQL = "INSERT INTO " + CONSIGNOR_TABLE_NAME + " (" + LAST_NAME_COLUMN_NAME + ", " + FIRST_NAME_COLUMN_NAME + ", " + PHONE_COLUMN_NAME + ", " + PROFIT_COLUMN_NAME + ") VALUES ('" + addConsignorLastName.getText() + "', '" + addConsignorFirstName.getText() + "', " + addConsignorPhone.getText() + ", 0)";
                try {
                    statement.executeUpdate(addConsignorSQL);
                } catch (SQLException sqle) {
                    System.out.println("There was an error adding a consignor. Printing stack trace...");
                    sqle.printStackTrace();
                }
            }
        });

        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String addRecordSQL = "INSERT INTO " + RECORDS_TABLE_NAME + "(" + CONSIGNOR_NUMBER_COLUMN_NAME + ", " + ARTIST_COLUMN_NAME + ", " + TITLE_COLUMN_NAME + ", " + PRICE_COLUMN_NAME + ", " + DAYS_IN_STORE_COLUMN_NAME + ", " + LOCATION_COLUMN_NAME + ") VALUES (" + addRecordConsignorNumber.getText() + ", '" + addRecordArtist.getText() + "', '" + addRecordTitle.getText() + "', " + addRecordPrice.getText() + ", 0, '" + addRecordLocation.getText() + "')";
                try {
                    if (entryInt(addRecordConsignorNumber.getText()) && consignorExists(addRecordConsignorNumber.getText())) {
                        if (entryDouble(addRecordPrice.getText())) {
                            statement.executeUpdate(addRecordSQL);
                        } else {
                            JOptionPane.showMessageDialog(null, "You must enter price with a period to differentiate dollars/cents and no dollar sign, like this: 9.99");
                        }
                    } else {
                        JOptionPane.showMessageDialog(null, "Either you didn't enter an integer (1, 2, 3, etc.) or that consignor does not exist. Please try again.");
                    }
                } catch (SQLException sqle) {
                    System.out.println("There was an error adding a record. Printing stack trace...");
                    sqle.printStackTrace();
                }
            }
        });

        removeConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String deleteConsignorSQL = "DELETE FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + "=" + removeConsignorNum.getText();
                try {
                    if (entryInt(removeConsignorNum.getText()) && consignorExists(removeConsignorNum.getText())) {
                            int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Consignor " + removeConsignorNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                            if (check == JOptionPane.YES_OPTION) {
                                statement.executeUpdate(deleteConsignorSQL);
                            }
                    } else {
                        JOptionPane.showMessageDialog(null, "Could not find Consignor. Did you enter an integer (1, 2, 3, etc.)? ");
                }
                } catch (SQLException sqle) {
                    System.out.println("There was an error deleting a consignor. Printing stack trace...");
                    sqle.printStackTrace();
                }
            }
        });

        // TODO store sale date
        removeRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String deleteRecordSQL = "DELETE FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + "=" + removeRecordNum.getText();
                try {
                    if (entryInt(removeRecordNum.getText()) && recordExists(removeRecordNum.getText())) {
                            int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Record " + removeRecordNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                            if (check == JOptionPane.YES_OPTION) {
                                String getCurrentConsignorSQL = "SELECT " + CONSIGNOR_NUMBER_COLUMN_NAME + " FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + "=" + removeRecordNum.getText();
                                ResultSet currentConsignorRS = statement.executeQuery(getCurrentConsignorSQL);
                                currentConsignorRS.next();
                                int currentConsignorNum = currentConsignorRS.getInt("ConsignorNum");
                                String getCurrentProfitSQL = "SELECT " + PROFIT_COLUMN_NAME + " FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + "=" + currentConsignorNum;
                                ResultSet consignorCurrentProfitRS = statement.executeQuery(getCurrentProfitSQL);
                                consignorCurrentProfitRS.next();
                                double consignorCurrentProfit = consignorCurrentProfitRS.getDouble("Profit");
                                double consignorCut = Double.parseDouble(removeRecordPrice.getText()) * CONSIGNOR_CUT;
                                double storeCut = Double.parseDouble(removeRecordPrice.getText()) - consignorCut;
                                sales.add(removeRecordNum.getText()); sales.add(removeRecordDate.getText()); sales.add(Double.toString(storeCut));
                                // TODO add storeCut somewhere
                                consignorCurrentProfit += consignorCut;
                                String updateConsignorProfit = "UPDATE " + CONSIGNOR_TABLE_NAME + " SET " + PROFIT_COLUMN_NAME + "=" + consignorCurrentProfit + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + "=" + currentConsignorNum;
                                statement.executeUpdate(updateConsignorProfit);
                                statement.executeUpdate(deleteRecordSQL);
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

        // TODO populate list, update on entry/removal, new window with info when clicked
        consignorsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

            }
        });

        // TODO populate list, update on entry/removal, new window with info when clicked
        recordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

            }
        });

        // Complete
        add1DayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String addOneDaySQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + DAYS_IN_STORE_COLUMN_NAME + "=" + DAYS_IN_STORE_COLUMN_NAME + "+1";
                    statement.executeUpdate(addOneDaySQL);
                } catch (SQLException sqle){
                    JOptionPane.showMessageDialog(null, "Error updating time in-store. Printing stack trace...");
                    sqle.printStackTrace();
                }
            }
        });

        // Complete
        add1WeekButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String addOneWeekSQL = "UPDATE " + RECORDS_TABLE_NAME + " SET " + DAYS_IN_STORE_COLUMN_NAME + "=" + DAYS_IN_STORE_COLUMN_NAME + "+7";
                    statement.executeUpdate(addOneWeekSQL);
                } catch (SQLException sqle) {
                    JOptionPane.showMessageDialog(null, "Error updating time in-store. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    //TODO
                    int entries = sales.size() / 3;
                    for (int x = 0; x < entries; x++) {
                        String updateTableSQL = "INSERT INTO " + SALES_TABLE_NAME + "(" + RECORD_NUMBER_COLUMN_NAME + ", " + SALE_PRICE_COLUMN_NAME + ", " + SALE_DATE_COLUMN_NAME + ") VALUES (" + sales.get(x*3) + ", " + sales.get(x*3+1) + ", " + sales.get(x*3+2) + ")";
                        statement.executeUpdate(updateTableSQL);
                    }
                } catch (SQLException sqle) {
                    JOptionPane.showMessageDialog(null, "Error updating Sales table. Printing stack trace.");
                    sqle.printStackTrace();
                }
            }
        });
    }

    // Complete
    boolean consignorExists(String userEntry) throws SQLException{
        String checkConsignorPresentQuery = "SELECT * FROM " + CONSIGNOR_TABLE_NAME + " WHERE " + CONSIGNOR_NUMBER_COLUMN_NAME + "=" + userEntry;
        ResultSet consignorsRS = statement.executeQuery(checkConsignorPresentQuery);
        return consignorsRS.next();
    }

    // Complete
    boolean recordExists(String userEntry) throws SQLException {
        String checkRecordPresentQuery = "SELECT * FROM " + RECORDS_TABLE_NAME + " WHERE " + RECORD_NUMBER_COLUMN_NAME + "=" + userEntry;
        ResultSet recordsRS = statement.executeQuery(checkRecordPresentQuery);
        return recordsRS.next();
    }

    // Complete
    boolean entryInt(String entryValue) {
        try {
            Integer.parseInt(entryValue);}
        catch (NumberFormatException nfe) {return false;}
        catch (NullPointerException npe) {return false;}
        return true;
    }

    // Complete
    boolean entryDouble(String entryValue) {
        try {
            Double.parseDouble(entryValue);}
        catch (NumberFormatException nfe) {return false;}
        catch (NullPointerException npe) {return false;}
        return true;
    }
}