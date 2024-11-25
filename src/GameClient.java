import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class GameClient extends JFrame {
    private GameBoard gameBoard;
    private NetworkManager networkManager;
    private int currentPlayer;
    private boolean isMyTurn;

    public GameClient(String host, int port, int currentPlayer) throws IOException {
        this.gameBoard = new GameBoard();
        this.networkManager = new NetworkManager(host, port);
        this.currentPlayer = currentPlayer;
        this.isMyTurn = currentPlayer == 1; // Первый игрок начинает

        setTitle("Gomoku");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        add(new GamePanel());

        new Thread(() -> {
            try {
                while (true) {
                    String message = networkManager.receiveMessage();
                    if (message != null) {
                        String[] parts = message.split(",");
                        int x = Integer.parseInt(parts[0]);
                        int y = Integer.parseInt(parts[1]);
                        gameBoard.makeMove(x, y, 3 - currentPlayer); // Противник
                        isMyTurn = true;
                        repaint();
                        if (gameBoard.checkWin(x, y)) {
                            JOptionPane.showMessageDialog(null, "Player " + (3 - currentPlayer) + " wins!");
                            System.exit(0);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private class GamePanel extends JPanel {
        public GamePanel() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (!isMyTurn) return;
                    int x = e.getX() / 40;
                    int y = e.getY() / 40;
                    if (gameBoard.makeMove(x, y, currentPlayer)) {
                        repaint();
                        if (gameBoard.checkWin(x, y)) {
                            JOptionPane.showMessageDialog(null, "Player " + currentPlayer + " wins!");
                            System.exit(0);
                        }
                        networkManager.sendMessage(x + "," + y);
                        isMyTurn = false;
                    }
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.BLACK);
            for (int i = 0; i < GameBoard.SIZE; i++) {
                g.drawLine(0, i * 40, GameBoard.SIZE * 40, i * 40);
                g.drawLine(i * 40, 0, i * 40, GameBoard.SIZE * 40);
            }
            int[][] board = gameBoard.getBoard();
            for (int i = 0; i < GameBoard.SIZE; i++) {
                for (int j = 0; j < GameBoard.SIZE; j++) {
                    if (board[i][j] == 1) {
                        g.setColor(Color.BLACK);
                        g.fillOval(i * 40 + 5, j * 40 + 5, 30, 30);
                    } else if (board[i][j] == 2) {
                        g.setColor(Color.WHITE);
                        g.fillOval(i * 40 + 5, j * 40 + 5, 30, 30);
                    }
                }
            }
        }
    }

    private static class GameBoard {
        private static final int SIZE = 15; // Размер поля
        private int[][] board;

        public GameBoard() {
            board = new int[SIZE][SIZE];
            for (int[] row : board) {
                Arrays.fill(row, 0); // 0 - пустая клетка, 1 - игрок 1, 2 - игрок 2
            }
        }

        public boolean makeMove(int x, int y, int player) {
            if (x >= 0 && x < SIZE && y >= 0 && y < SIZE && board[x][y] == 0) {
                board[x][y] = player;
                return true;
            }
            return false;
        }

        public boolean checkWin(int x, int y) {
            // Проверка всех направлений на наличие пяти подряд
            return checkDirection(x, y, 1, 0) || // Горизонталь
                    checkDirection(x, y, 0, 1) || // Вертикаль
                    checkDirection(x, y, 1, 1) || // Диагональ вниз вправо
                    checkDirection(x, y, 1, -1);  // Диагональ вниз влево
        }

        private boolean checkDirection(int x, int y, int dx, int dy) {
            int player = board[x][y];
            int count = 1;
            for (int i = 1; i < 5; i++) {
                int nx = x + dx * i;
                int ny = y + dy * i;
                if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE && board[nx][ny] == player) {
                    count++;
                } else {
                    break;
                }
            }
            for (int i = 1; i < 5; i++) {
                int nx = x - dx * i;
                int ny = y - dy * i;
                if (nx >= 0 && nx < SIZE && ny >= 0 && ny < SIZE && board[nx][ny] == player) {
                    count++;
                } else {
                    break;
                }
            }
            return count >= 5;
        }

        public int[][] getBoard() {
            return board;
        }
    }

    private static class NetworkManager {
        private Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        public NetworkManager(String host, int port) throws IOException {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public String receiveMessage() throws IOException {
            return in.readLine();
        }

        public void close() throws IOException {
            in.close();
            out.close();
            socket.close();
        }
    }


}