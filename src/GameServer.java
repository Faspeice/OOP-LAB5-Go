import java.io.*;
import java.net.*;

public class GameServer {
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345);
        System.out.println("Server started...");

        Socket player1 = serverSocket.accept();
        System.out.println("Player 1 connected...");
        Socket player2 = serverSocket.accept();
        System.out.println("Player 2 connected...");

        new Thread(new PlayerHandler(player1, player2)).start();
        new Thread(new PlayerHandler(player2, player1)).start();
    }

    private static class PlayerHandler implements Runnable {
        private Socket player;
        private Socket opponent;

        public PlayerHandler(Socket player, Socket opponent) {
            this.player = player;
            this.opponent = opponent;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(player.getInputStream()));
                PrintWriter out = new PrintWriter(opponent.getOutputStream(), true);
                String message;
                while ((message = in.readLine()) != null) {
                    out.println(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    player.close();
                    opponent.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}