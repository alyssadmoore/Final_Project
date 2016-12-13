import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class MusicStoreGUI extends JFrame{
    JPanel rootPanel;
    JTextField addConsignorFirstName;
    JTextField addConsignorLastName;
    JTextField addConsignorPhone;
    JButton addConsignorButton;
    JTextField addRecordArtist;
    JTextField addRecordTitle;
    JTextField addRecordPrice;
    JButton addRecordButton;
    JButton removeConsignorButton;
    JTextField removeRecordNum;
    JTextField removeRecordPrice;
    JButton removeRecordButton;
    JList<String> consignorsList;
    JList<String> recordsList;
    JTextField addRecordLocation;
    JButton quitButton;
    private JComboBox<Integer> consignorNumComboBox;
    private JTabbedPane listTabs;
    private JPanel Consignors;
    private JPanel Records;
    private JTabbedPane addTab;
    private JTabbedPane removeTab;
    private JPanel AddConsignor;
    private JPanel AddRecord;
    private JButton searchConsignorButton;
    private JButton statisticsButton;
    private JComboBox<Integer> payAConsignorComboBox;
    private JTextField payAConsignorAmount;
    private JButton payAConsignorButton;
    private JComboBox<Integer> addAConsignorComboBox;
    private JTextField searchConsignorFirstName;
    private JButton searchRecordButton;
    private JTextField searchRecordArtist;
    private JTextField searchConsignorLastName;
    private JTextField searchConsignorPhone;
    private JTextField searchRecordTitle;
    private JCheckBox firstORLast;
    private JCheckBox phoneORFirst;
    private JCheckBox lastORPhone;
    private JCheckBox artistORTitle;
    private JTabbedPane updateTabbedPane;
    private JButton changeConsignorSubmitButton;
    private JTextField updateRecordField;
    private JTextField changeConsignorNewField;
    private JButton submitUpdateRecordButton;
    private JTextField updateRecordNewValue;
    private JComboBox<String> updateComboBox;
    private JComboBox<Integer> updateConsignorNumComboBox;
    private JComboBox<String> updateRecordComboBox;

    private static ArrayList<String> associatedRecords = new ArrayList<String>();
    private static DefaultListModel<String> consignorModel = new DefaultListModel<>();
    private static DefaultListModel<String> recordModel = new DefaultListModel<>();
    private static DefaultComboBoxModel<String> changeRecordComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Integer> addConsignorNumComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Integer> payConsignorNumComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Integer> removeConsignorNumComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Integer> updateConsignorNumComboBoxModel = new DefaultComboBoxModel<>();

    private Controller controller;

    private final static String CONSIGNOR_TABLE_NAME = "Consignors";
    private final static String RECORDS_TABLE_NAME = "Records";
    private final static String CONSIGNOR_NUMBER_COLUMN_NAME = "ConsignorNum";
    private final static String RECORD_NUMBER_COLUMN_NAME = "RecordNum";
    private final static String ARTIST_COLUMN_NAME = "Artist";
    private final static String TITLE_COLUMN_NAME = "Title";
    private final static String PRICE_COLUMN_NAME = "Price";
    private final static String LOCATION_COLUMN_NAME = "Location";
    private final static String LAST_NAME_COLUMN_NAME = "LastName";
    private final static String FIRST_NAME_COLUMN_NAME = "FirstName";
    private final static String PHONE_COLUMN_NAME = "Phone";

    MusicStoreGUI(Controller controller) {
        super("Record Store Manager");
        this.controller = controller;
        setContentPane(rootPanel);
        consignorsList.setModel(consignorModel);
        recordsList.setModel(recordModel);
        consignorNumComboBox.setModel(removeConsignorNumComboBoxModel);
        payAConsignorComboBox.setModel(payConsignorNumComboBoxModel);
        addAConsignorComboBox.setModel(addConsignorNumComboBoxModel);
        updateRecordComboBox.setModel(changeRecordComboBoxModel);
        updateConsignorNumComboBox.setModel(updateConsignorNumComboBoxModel);
        listTabs.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        listTabs.setTitleAt(1, RECORDS_TABLE_NAME);
        listTabs.setTitleAt(2, "Search " + CONSIGNOR_TABLE_NAME);
        listTabs.setTitleAt(3, "Search " + RECORDS_TABLE_NAME);
        addTab.setTitleAt(0, "Add " + CONSIGNOR_TABLE_NAME);
        addTab.setTitleAt(1, "Add " + RECORDS_TABLE_NAME);
        addTab.setTitleAt(2, "Remove " + CONSIGNOR_TABLE_NAME);
        addTab.setTitleAt(3, "Sell " + RECORDS_TABLE_NAME);
        updateTabbedPane.setTitleAt(0, "Edit " + CONSIGNOR_TABLE_NAME);
        updateTabbedPane.setTitleAt(1, "Edit " + RECORDS_TABLE_NAME);
        updateComboBox.addItem(FIRST_NAME_COLUMN_NAME);
        updateComboBox.addItem(LAST_NAME_COLUMN_NAME);
        updateComboBox.addItem(PHONE_COLUMN_NAME);
        updateRecordComboBox.addItem(ARTIST_COLUMN_NAME);
        updateRecordComboBox.addItem(TITLE_COLUMN_NAME);
        updateRecordComboBox.addItem(PRICE_COLUMN_NAME);
        updateRecordComboBox.addItem(LOCATION_COLUMN_NAME);

        ArrayList consignor = controller.populateConsignorList();
        for (int x = 0; x < consignor.size(); x++) {
            consignorModel.addElement((String)consignor.get(x));
        }

        ArrayList record = controller.populateRecordList();
        for (int x = 0; x < record.size(); x++) {
            if (!recordModel.contains(record.get(x))) {
                recordModel.addElement((String)record.get(x));
            }
        }

        controller.updateDaysInStore();
        controller.alertDaysInStore();
        controller.checkNumCopiesMax();
        ArrayList comboList = controller.updateComboBox();
        updateComboBoxes(comboList);
        pack();
        createListeners();
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    void createListeners() {

        // Adding a consignor to the database
        addConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addConsignorLastName.getText().trim().length() == 0 || addConsignorFirstName.getText().trim().length() == 0 || addConsignorPhone.getText().trim().length() == 0) {
                    JOptionPane.showMessageDialog(null, "Please enter a first name, last name, and phone number to add a new consignor.");
                } else {
                    controller.addConsignor(addConsignorLastName.getText(), addConsignorFirstName.getText(), addConsignorPhone.getText());
                    updateComboBoxes(controller.updateComboBox());
                    updateConsignorsList(controller.populateConsignorList());
                    addConsignorLastName.setText("");
                    addConsignorFirstName.setText("");
                    addConsignorPhone.setText("");
                    JOptionPane.showMessageDialog(null, "Consignor added successfully");
                }
            }
        });

        // Adding a record to the database
        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Validation.entryDouble(addRecordPrice.getText())) {
                    if (addRecordArtist.getText().trim().length() == 0 || addRecordTitle.getText().trim().length() == 0) {
                        JOptionPane.showMessageDialog(null, "Please enter both an artist and a title to add a new record.");
                    } else {
                        controller.addRecord((Integer) addAConsignorComboBox.getSelectedItem(), addRecordArtist.getText(), addRecordTitle.getText(), Double.parseDouble(addRecordPrice.getText()), addRecordLocation.getText());
                        JOptionPane.showMessageDialog(null, "Record added successfully");
                        updateRecordsList(controller.populateRecordList());
                        addRecordArtist.setText("");
                        addRecordTitle.setText("");
                        addRecordPrice.setText("");
                        addRecordLocation.setText("");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "You must enter price with a period to differentiate dollars/cents and no dollar sign, like this: 9.99");
                }
            }
        });

        // Removing a consignor from the database
        removeConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Consignor " + consignorNumComboBox.getSelectedItem() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                if (check == JOptionPane.YES_OPTION) {
                    controller.removeConsignor((Integer) consignorNumComboBox.getSelectedItem());
                    updateConsignorsList(controller.populateConsignorList());
                    updateComboBoxes(controller.updateComboBox());
                    JOptionPane.showMessageDialog(null, "Consignor deleted successfully");
                }
            }
        });

        // Removing a record from the database
        removeRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Validation.entryInt(removeRecordNum.getText()) && controller.recordExists(removeRecordNum.getText()) && removeRecordPrice.getText().trim().length()>0) {
                    int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Record " + removeRecordNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                    if (check == JOptionPane.YES_OPTION) {
                        controller.removeRecord(Integer.parseInt(removeRecordNum.getText()), Double.parseDouble(removeRecordPrice.getText()));
                        updateRecordsList(controller.populateRecordList());
                        removeRecordNum.setText("");
                        removeRecordPrice.setText("");
                        JOptionPane.showMessageDialog(null, "Record removed successfully");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Either that record doesn't exist, you entered a non-integer value, or you didn't enter a valid sale price. Please try again.");
                }
            }
        });

        // Generating a new window of information about the consignor clicked on
        consignorsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                associatedRecords.clear();
                int consignorNum = 0;
                try {
                    consignorNum = controller.clickConsignor(consignorsList.getSelectedValue());
                    if (consignorNum != 0) {
                        ArrayList alRecord = controller.findAssociatedRecords(consignorNum);
                        for (int x = 0; x < alRecord.size(); x++) {
                            associatedRecords.add((String) alRecord.get(x));
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please select a consignor to view more info");
                }
                if (consignorNum != 0) {
                    JOptionPane.showMessageDialog(null, controller.getConsignorInfo(consignorNum) + "\n\nAssociated records: " + associatedRecords);
                }
            }
        });

        // When a record in the list is clicked, a popup for each record with the same name will appear
        recordsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                try {
                    String fullAlbum = recordsList.getSelectedValue().toString();
                    ArrayList albumInfo = controller.clickRecord(fullAlbum);
                    if (albumInfo.size()!=0) {
                        for (int x = 0; x < albumInfo.size(); x++) {
                            JOptionPane.showMessageDialog(null, albumInfo.get(x));
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please click a record to view info");
                }
            }
        });

        // Generating a popup with statistics
        statisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String statistics = controller.getStatistics();
                JOptionPane.showMessageDialog(null, statistics, "Statistics", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        // Updating a consignor's amount owed and profit in the database
        payAConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Validation.entryDouble(payAConsignorAmount.getText())){
                    Double payment = Double.parseDouble(payAConsignorAmount.getText());
                    int consignor = (Integer)payAConsignorComboBox.getSelectedItem();
                    controller.payAConsignor(consignor, payment);
                    payAConsignorAmount.setText("");
                } else {
                    JOptionPane.showMessageDialog(null, "It seems you didn't enter the right data type. Enter payment with a period separating dollars and cents with no other symbols, like this: 10.00");
                }
            }
        });

        // Searching the consignors table with given arguments
        searchConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ArrayList<String> consignorInfo = new ArrayList<>();
                String first = searchConsignorFirstName.getText().trim();
                String last = searchConsignorLastName.getText().trim();
                String phone = searchConsignorPhone.getText().trim();
                boolean firstOrLast = firstORLast.isSelected();
                boolean lastOrPhone = lastORPhone.isSelected();
                boolean phoneOrFirst = phoneORFirst.isSelected();
                boolean moreInfoNeeded = false;
                if (first.length() == 0) {
                    first = null;
                }
                if (last.length() == 0) {
                    last = null;
                }
                if (phone.length() == 0) {
                    phone = null;
                }
                if (first == null && last == null && phone == null) {
                    JOptionPane.showMessageDialog(null, "Please enter a first name, last name, or phone to search Consignors.");
                    moreInfoNeeded = true;
                }
                if (firstOrLast && (first == null || last == null)) {
                    JOptionPane.showMessageDialog(null, "Please enter both a first and last name, or uncheck the \'First OR Last\' button");
                    moreInfoNeeded = true;
                }
                if (lastOrPhone && (last == null || phone == null)) {
                    JOptionPane.showMessageDialog(null, "Please enter both a last name and phone number, or uncheck the \'Last OR Phone\' button");
                    moreInfoNeeded = true;
                }
                if (phoneOrFirst && (phone == null || first == null)) {
                    JOptionPane.showMessageDialog(null, "Please enter both a phone number and first name, or uncheck the \'Phone OR First\' button");
                    moreInfoNeeded = true;
                }
                if (!moreInfoNeeded) {
                    consignorInfo = controller.findConsignorInfo(first, last, phone, firstOrLast, lastOrPhone, phoneOrFirst);
                    if (consignorInfo.size() == 0) {
                        JOptionPane.showMessageDialog(null, "Sorry, no results");
                    } else {
                        for (int x = 0; x < consignorInfo.size(); x++) {
                            JOptionPane.showMessageDialog(null, consignorInfo.get(x), "Result " + (x + 1) + " of " + consignorInfo.size(), JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                }
                searchConsignorFirstName.setText("");
                searchConsignorLastName.setText("");
                searchConsignorPhone.setText("");
                firstORLast.setSelected(false);
                lastORPhone.setSelected(false);
                phoneORFirst.setSelected(false);
            }
        });

        // Searching the Records table with given arguments
        searchRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String artist = searchRecordArtist.getText().trim();
                String title = searchRecordTitle.getText().trim();
                ArrayList recordInfo = new ArrayList();
                boolean or = artistORTitle.isSelected();
                boolean needMoreEntry = false;
                if (artist.length() == 0) {
                    artist = null;
                }
                if (title.length() == 0) {
                    title = null;
                }
                if (artist == null && title == null){
                    JOptionPane.showMessageDialog(null, "No entry detected; please enter either a title or artist to continue.");
                    needMoreEntry = true;
                } else {
                    recordInfo = controller.findRecordNum(artist, title, or);
                }
                if (!needMoreEntry) {
                    if (recordInfo.size() == 0) {
                        JOptionPane.showMessageDialog(null, "Sorry, no results.");
                    } else {
                        for (int x = 0; x < recordInfo.size(); x++) {
                            JOptionPane.showMessageDialog(null, recordInfo.get(x), "Result " + (x + 1) + " of " + recordInfo.size(), JOptionPane.PLAIN_MESSAGE);
                        }
                    }
                }
                searchRecordArtist.setText("");
                searchRecordTitle.setText("");
                artistORTitle.setSelected(false);
            }
        });

        // Updating a consignor's information
        changeConsignorSubmitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int consignorNum = (Integer)updateConsignorNumComboBox.getSelectedItem();
                String toUpdate = (String)updateComboBox.getSelectedItem();
                String newValue = changeConsignorNewField.getText().trim();
                boolean moreInfoNeeded = false;
                if (newValue.length() == 0) {
                    JOptionPane.showMessageDialog(null, "Please enter a new value to update.");
                    moreInfoNeeded = true;
                }
                if (!moreInfoNeeded) {
                    controller.updateConsignor(consignorNum, newValue, toUpdate);
                    updateConsignorsList(controller.populateConsignorList());
                    changeConsignorNewField.setText("");
                    JOptionPane.showMessageDialog(null, "Update successful");
                }
            }
        });

        // Updating a record's information
        submitUpdateRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int recordNum = 0;
                if (Validation.entryInt(updateRecordField.getText())) {
                    recordNum = Integer.parseInt(updateRecordField.getText());
                }
                String newVariable = updateRecordNewValue.getText();
                String toChange = (String)updateRecordComboBox.getSelectedItem();
                controller.updateRecord(recordNum, newVariable, toChange);
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    // Updates the consignor num combo boxes from database
    private static void updateComboBoxes(ArrayList comboBoxItems) {
        addConsignorNumComboBoxModel.removeAllElements();
        removeConsignorNumComboBoxModel.removeAllElements();
        payConsignorNumComboBoxModel.removeAllElements();
        updateConsignorNumComboBoxModel.removeAllElements();
        for (int x = 0; x < comboBoxItems.size(); x++) {
            addConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
            removeConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
            payConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
            updateConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
        }
    }

    // Redraws the consignors JList from database
    private void updateConsignorsList(ArrayList<String> list) {
        consignorModel.removeAllElements();
        for (int x = 0; x < list.size(); x++) {
            consignorModel.addElement(list.get(x));
        }
    }

    // Redraws the records JList from database
    private void updateRecordsList(ArrayList<String> list) {
        recordModel.removeAllElements();
        for (int x = 0; x < list.size(); x++) {
            recordModel.addElement(list.get(x));
        }
    }
}