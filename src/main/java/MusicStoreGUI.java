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
    JList consignorsList;
    JList recordsList;
    JTextField addRecordLocation;
    JButton quitButton;
    private JComboBox consignorNumComboBox;
    private JTabbedPane listTabs;
    private JPanel Consignors;
    private JPanel Records;
    private JTabbedPane addTab;
    private JTabbedPane removeTab;
    private JPanel AddConsignor;
    private JPanel AddRecord;
    private JButton searchConsignorButton;
    private JButton statisticsButton;
    private JComboBox payAConsignorComboBox;
    private JTextField payAConsignorAmount;
    private JButton payAConsignorButton;
    private JComboBox addAConsignorComboBox;
    private JTextField searchConsignorFirstName;
    private JButton searchRecordButton;
    private JTabbedPane searchTab;
    private JTextField searchRecordArtist;
    private JTextField searchConsignorLastName;
    private JTextField searchConsignorPhone;
    private JTextField searchRecordTitle;
    private JCheckBox firstORLast;
    private JCheckBox phoneORFirst;
    private JCheckBox lastORPhone;
    private JCheckBox artistORTitle;

    private static ArrayList<String> associatedRecords = new ArrayList<String>();
    private static DefaultListModel<String> consignorModel = new DefaultListModel<>();
    private static DefaultListModel<String> recordModel = new DefaultListModel<>();
    private static DefaultComboBoxModel<Integer> addConsignorNumComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Integer> payConsignorNumComboBoxModel = new DefaultComboBoxModel<>();
    private static DefaultComboBoxModel<Integer> removeConsignorNumComboBoxModel = new DefaultComboBoxModel<>();

    private Controller controller;

    private final static String CONSIGNOR_TABLE_NAME = "Consignors";
    private final static String RECORDS_TABLE_NAME = "Records";

    MusicStoreGUI(Controller controller) {
        super("Record Store Manager");
        this.controller = controller;
        setContentPane(rootPanel);
        consignorsList.setModel(consignorModel);
        recordsList.setModel(recordModel);
        consignorNumComboBox.setModel(removeConsignorNumComboBoxModel);
        payAConsignorComboBox.setModel(payConsignorNumComboBoxModel);
        addAConsignorComboBox.setModel(addConsignorNumComboBoxModel);
        listTabs.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        listTabs.setTitleAt(1, RECORDS_TABLE_NAME);
        addTab.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        addTab.setTitleAt(1, RECORDS_TABLE_NAME);
        removeTab.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        removeTab.setTitleAt(1, RECORDS_TABLE_NAME);
        searchTab.setTitleAt(0, CONSIGNOR_TABLE_NAME);
        searchTab.setTitleAt(1, RECORDS_TABLE_NAME);

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

        addConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (addConsignorLastName.getText().trim().length() == 0 || addConsignorFirstName.getText().trim().length() == 0 || addConsignorPhone.getText().trim().length() == 0) {
                    JOptionPane.showMessageDialog(null, "Please enter a first name, last name, and phone number to add a new consignor.");
                } else {
                    controller.addConsignor(addConsignorLastName.getText(), addConsignorFirstName.getText(), addConsignorPhone.getText());
                    consignorModel.addElement(addConsignorLastName.getText() + ", " + addConsignorFirstName.getText());
                    updateComboBoxes(controller.updateComboBox());
                    addConsignorLastName.setText("");
                    addConsignorFirstName.setText("");
                    addConsignorPhone.setText("");
                    JOptionPane.showMessageDialog(null, "Consignor added successfully");
                }
            }
        });

        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Validation.entryDouble(addRecordPrice.getText())) {
                    if (addRecordArtist.getText().trim().length() == 0 || addRecordTitle.getText().trim().length() == 0) {
                        JOptionPane.showMessageDialog(null, "Please enter both an artist and a title to add a new record.");
                    } else {
                        controller.addRecord((Integer) addAConsignorComboBox.getSelectedItem(), addRecordArtist.getText(), addRecordTitle.getText(), Double.parseDouble(addRecordPrice.getText()), addRecordLocation.getText());
                        JOptionPane.showMessageDialog(null, "Record added successfully");
                        if (!recordModel.contains(addRecordArtist.getText() + ", " + addRecordTitle.getText())) {
                            recordModel.addElement(addRecordArtist.getText() + ", " + addRecordTitle.getText());
                        }
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

        removeConsignorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Consignor " + consignorNumComboBox.getSelectedItem() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                if (check == JOptionPane.YES_OPTION) {
                    String fullname = controller.findConsignorName((Integer) consignorNumComboBox.getSelectedItem());
                    consignorModel.removeElement(fullname);
                    controller.removeConsignor((Integer) consignorNumComboBox.getSelectedItem());
                    updateComboBoxes(controller.updateComboBox());
                    JOptionPane.showMessageDialog(null, "Consignor deleted successfully");
                }
            }
        });

        removeRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (Validation.entryInt(removeRecordNum.getText()) && DB.recordExists(removeRecordNum.getText()) && removeRecordPrice.getText().trim().length()>0) {
                    int check = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete Record " + removeRecordNum.getText() + "?", "Are you sure?", JOptionPane.YES_NO_OPTION);
                    if (check == JOptionPane.YES_OPTION) {
                        String fullAlbum = controller.findRecordTitle(Integer.parseInt(removeRecordNum.getText()));
                        controller.removeRecord(Integer.parseInt(removeRecordNum.getText()), Double.parseDouble(removeRecordPrice.getText()));
                        if (!fullAlbum.equals("null")) {
                            recordModel.removeElement(fullAlbum);
                        }
                        removeRecordNum.setText("");
                        removeRecordPrice.setText("");
                        JOptionPane.showMessageDialog(null, "Record removed successfully");
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Either that record doesn't exist, you entered a non-integer value, or you didn't enter a valid sale price. Please try again.");
                }
            }
        });

        consignorsList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                associatedRecords.clear();
                int consignorNum = 0;
                try {
                    consignorNum = controller.clickConsignor(consignorsList.getSelectedValue().toString());
                    if (consignorNum != 0) {
                        ArrayList alRecord = controller.findAssociatedRecords(consignorNum);
                        for (int x = 0; x < alRecord.size(); x++) {
                            associatedRecords.add((String) alRecord.get(x));
                        }
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Please select a consignor to view more info");
                }
                // TODO add etc metadata(?)
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

        statisticsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String statistics = controller.getStatistics();
                JOptionPane.showMessageDialog(null, statistics, "Statistics", JOptionPane.INFORMATION_MESSAGE);
            }
        });

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
            }
        });

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
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private static void updateComboBoxes(ArrayList comboBoxItems) {
        addConsignorNumComboBoxModel.removeAllElements();
        removeConsignorNumComboBoxModel.removeAllElements();
        payConsignorNumComboBoxModel.removeAllElements();
        for (int x = 0; x < comboBoxItems.size(); x++) {
            addConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
            removeConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
            payConsignorNumComboBoxModel.addElement((Integer)comboBoxItems.get(x));
        }
    }
}