import javax.swing.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor TCP para chat con interfaz gráfica
 * Permite gestionar múltiples conexiones de clientes
 */
public class ServerGUI extends JFrame {
    private JTextArea logArea;
    private JList<String> clientsList;
    private DefaultListModel<String> clientsModel;
    private ServerSocket serverSocket;
    private Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private int port = 5555;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private boolean running = false;

    public ServerGUI() {
        setTitle("Servidor de Chat - Interfaz Gráfica");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JLabel titleLabel = new JLabel("Servidor de Chat TCP", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Área de log
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        JScrollPane scrollLog = new JScrollPane(logArea);

        // Panel de clientes
        JPanel clientsPanel = new JPanel(new BorderLayout());
        clientsPanel.setBorder(BorderFactory.createTitledBorder("Clientes Conectados"));
        clientsModel = new DefaultListModel<>();
        clientsList = new JList<>(clientsModel);
        clientsPanel.add(new JScrollPane(clientsList), BorderLayout.CENTER);

        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollLog, clientsPanel);
        splitPane.setDividerLocation(450);
        mainPanel.add(splitPane, BorderLayout.CENTER);

        // Panel de botones
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
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                log("✓ Servidor detenido");
            }
        } catch (IOException e) {
            log("✗ Error al detener: " + e.getMessage());
        }
    }

    public void addClient(ClientHandler client) {
        clientsModel.addElement(client.getUsername());
        log("[" + getTime() + "] ✓ " + client.getUsername() + " conectado");
        broadcast("✓ " + client.getUsername() + " entró al chat");
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
        clientsModel.removeElement(client.getUsername());
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

    /**
     * Maneja la comunicación con un cliente individual
     */
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String username;
        private ServerGUI server;

        public ClientHandler(Socket socket, ServerGUI server) throws IOException {
            this.socket = socket;
            this.server = server;
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
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
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        SwingUtilities.invokeLater(() -> new ServerGUI());
    }
}
