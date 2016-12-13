// database: MusicStore

import java.util.ArrayList;

public class Controller {

    private static MusicStoreGUI gui;
    private static DB db;

    public static void main(String[] args) {

        Controller controller = new Controller();
        controller.start();
    }

    private void start() {

        db = new DB();
        db.setup();
        gui = new MusicStoreGUI(this);

    }

    void addConsignor(String last, String first, String phone) {
        db.addConsignor(last, first, phone);
    }

    void addRecord(int consignor, String artist, String title, double price, String location) {
        db.addRecord(consignor, artist, title, price, location);
    }

    boolean recordExists(String userEntry) {
        return db.recordExists(userEntry);
    }

    void removeConsignor(int consignor) {
        db.removeConsignor(consignor);
    }

    void removeRecord(int record, double price) {
        db.removeRecord(record, price);
    }

    ArrayList<String> populateRecordList() {
        return db.populateRecordList();
    }

    ArrayList<String> populateConsignorList() {
        return db.populateConsignorList();
    }

    ArrayList<Integer> updateComboBox() {
        return db.updateComboBox();
    }

    void updateDaysInStore() {
        db.updateDaysInStore();
    }

    void alertDaysInStore() {
        db.alertDaysInStore();
    }

    void checkNumCopiesMax() {
        db.checkNumCopiesMax();
    }

    String getStatistics() {
        return db.getStatistics();
    }

    void payAConsignor(int consignor, double payment) {
        db.payAConsignor(consignor, payment);
    }

    int clickConsignor(String consignor) {
        return db.clickConsignor(consignor);
    }

    ArrayList<String> clickRecord(String record) {
        return db.clickRecord(record);
    }

    ArrayList<String> findAssociatedRecords(int consignor) {
        return db.findAssociatedRecords(consignor);
    }

    String getConsignorInfo(int consignor) {
        return db.getConsignorInfo(consignor);
    }

    ArrayList<String> findRecordNum(String artist, String title, boolean or) {
        return db.findRecordInfo(artist, title, or);
    }

    ArrayList<String> findConsignorInfo(String first, String last, String phone, boolean firstOrLast, boolean lastOrPhone, boolean phoneOrFirst) {
        return db.findConsignorInfo(first, last, phone, firstOrLast, lastOrPhone, phoneOrFirst);
    }

    void updateConsignor(int consignorNum, String newVariable, String toUpdate) {
        db.updateConsignor(consignorNum, newVariable, toUpdate);
    }

    void updateRecord(int recordNum, String newVariable, String toUpdate) {
        db.updateRecord(recordNum, newVariable, toUpdate);
    }
}