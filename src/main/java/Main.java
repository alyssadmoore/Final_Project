// database: MusicStore

public class Main {

    public static void main(String[] args) {

        if (!Startup.setup()) {
            System.exit(1);
        } else {
            new MusicStoreGUI();
        }
    }
}