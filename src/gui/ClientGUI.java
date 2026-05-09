import javax.swing.*;
import java.io.*;
import java.net.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Cliente TCP para chat con interfaz gráfica (Swing)
 * Permite conectarse a un servidor y chatear visualmente
 */
public class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private JButton connectButton;
    private JTextField hostField;
    private JSpinner portSpinner;
    private JTextField usernameField;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private boolean connected = false;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientGUI() {
        setTitle("Cliente de Chat");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);
        setResizable(true);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel connectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        connectionPanel.setBorder(BorderFactory.createTitledBorder("Conexión"));

        connectionPanel.add(new JLabel("Host:"));
        hostField = new JTextField("localhost", 15);
        connectionPanel.add(hostField);

        connectionPanel.add(new JLabel("Puerto:"));
        portSpinner = new JSpinner(new SpinnerNumberModel(5555, 1, 65535, 1));
        connectionPanel.add(portSpinner);

        connectionPanel.add(new JLabel("Usuario:"));
        usernameField = new JTextField(15);
        connectionPanel.add(usernameField);

        connectButton = new JButton("Conectar");
        connectButton.addActionListener(e -> connect());
        connectionPanel.add(connectButton);

        mainPanel.add(connectionPanel, BorderLayout.NORTH);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        JScrollPane scrollChat = new JScrollPane(chatArea);
        mainPanel.add(scrollChat, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messageField.setEnabled(false);
        messageField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Enviar");
        sendButton.setEnabled(false);
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);

        add(mainPanel);
        setVisible(true);
    }

    private void connect() {
        try {
            String host = hostField.getText();
            int port = (int) portSpinner.getValue();
            username = usernameField.getText();

            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingresa un nombre de usuario", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            socket = new Socket(host, port);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            writer.println(username);

            connected = true;
            hostField.setEnabled(false);
            portSpinner.setEnabled(false);
            usernameField.setEnabled(false);
            connectButton.setEnabled(false);
            messageField.setEnabled(true);
            sendButton.setEnabled(true);

            addMessage("[SISTEMA] Conectado a " + host + ":" + port);

            new Thread(this::receiveMessages).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error de conexión: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void sendMessage() {
        String message = messageField.getText().trim();
        if (!message.isEmpty() && connected) {
            writer.println(message);
            messageField.setText("");
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null && connected) {
                addMessage("[" + getTime() + "] " + message);
            }
        } catch (IOException e) {
            if (connected) {
                addMessage("[SISTEMA] Desconectado del servidor");
                disconnect();
            }
        }
    }

    private void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            hostField.setEnabled(true);
            portSpinner.setEnabled(true);
            usernameField.setEnabled(true);
            connectButton.setEnabled(true);
            messageField.setEnabled(false);
            sendButton.setEnabled(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatArea.append(message + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    private String getTime() {
        return LocalDateTime.now().format(formatter);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ClientGUI());
    }
}
