import javax.swing.*;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 12345;

        SwingUtilities.invokeLater(() -> {
            try {
                new GameClient(host, port, 1).setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        SwingUtilities.invokeLater(() -> {
            try {
                new GameClient(host, port, 2).setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
