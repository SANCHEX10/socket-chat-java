package gui;

import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JList<String> clientsList;
    private DefaultListModel<String> clientsModel;
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final int port = 5555;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private volatile boolean running = false;

    public ServerGUI() {
        setTitle("Servidor de Chat - Interfaz Gráfica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("Servidor de Chat TCP", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(logArea);

        JPanel clientsPanel = new JPanel(new BorderLayout());
        clientsPanel.setBorder(BorderFactory.createTitledBorder("Clientes Conectados"));
        clientsModel = new DefaultListModel<>();
        clientsList = new JList<>(clientsModel);
        clientsPanel.add(new JScrollPane(clientsList), BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLog, clientsPanel);
        splitPane.setDividerLocation(450);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton startButton = new JButton("Iniciar Servidor");
        JButton stopButton = new JButton("Detener Servidor");

        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void startServer() {
        if (running) return;
        running = true;

        new Thread(() -> {
            try {
                serverSocket = new ServerSocket(port);
                log("✓ Servidor iniciado en puerto " + port);

                while (running) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket, this);
                    clients.add(handler);
                    new Thread(handler).start();
                }
            } catch (IOException e) {
                if (running) {
                    log("✗ Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void stopServer() {
        running = false;
        closeAllClients();
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log("✓ Servidor detenido");
            }
        } catch (IOException e) {
            log("✗ Error al detener: " + e.getMessage());
        }
    }

    private void closeAllClients() {
        for (ClientHandler client : clients) {
            client.close();
        }
        clients.clear();
        SwingUtilities.invokeLater(clientsModel::clear);
    }

    public void addClient(ClientHandler client) {
        SwingUtilities.invokeLater(() -> clientsModel.addElement(client.getUsername()));
        log("[" + getTime() + "] ✓ " + client.getUsername() + " conectado");
        broadcast("✓ " + client.getUsername() + " entró al chat");
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        SwingUtilities.invokeLater(() -> clientsModel.removeElement(client.getUsername()));
        log("[" + getTime() + "] ✗ " + client.getUsername() + " desconectado");
        broadcast("✗ " + client.getUsername() + " salió del chat");
    }

    public void broadcast(String message) {
        log("[BROADCAST] " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    private void log(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private String getTime() {
        return LocalDateTime.now().format(formatter);
    }

    public static class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter writer;
        private final BufferedReader reader;
        private final String username;
        private final ServerGUI server;

        public ClientHandler(Socket socket, ServerGUI server) throws IOException {
            this.socket = socket;
            this.server = server;
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.username = reader.readLine();
        }

        @Override
        public void run() {
            try {
                server.addClient(this);
                String message;
                while ((message = reader.readLine()) != null) {
                    server.broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                // Conexión cerrada
            } finally {
                server.removeClient(this);
                close();
            }
        }

        public void close() {
            try {
                if (!socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void sendMessage(String message) {
            writer.println(message);
        }

        public String getUsername() {
            return username;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ServerGUI::new);
    }
}
