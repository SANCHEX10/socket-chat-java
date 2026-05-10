package cli;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * Cliente TCP para chat con interfaz CLI
 * Permite conectarse a un servidor y chatear interactivamente
 */
public class ClientCLI {
    private String host;
    private int port;
    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private String username;
    private boolean connected = false;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ClientCLI() {
        printHeader();
    }

    private void printHeader() {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║   CLIENTE DE CHAT - CLI MODE       ║");
        System.out.println("╚════════════════════════════════════╝\n");
    }

    public void connect() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Host [localhost]: ");
        String hostInput = scanner.nextLine().trim();
        host = hostInput.isEmpty() ? "localhost" : hostInput;

        System.out.print("Puerto [5555]: ");
        String portInput = scanner.nextLine().trim();
        port = portInput.isEmpty() ? 5555 : Integer.parseInt(portInput);

        System.out.print("Tu nombre de usuario: ");
        username = scanner.nextLine().trim();

        try {
            socket = new Socket(host, port);
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            writer.println(username);

            connected = true;
            System.out.println("\n✓ Conectado a " + host + ":" + port);
            System.out.println("Escribe 'salir' para desconectar\n");

            new Thread(this::receiveMessages).start();
            sendMessages(scanner);

        } catch (IOException e) {
            System.out.println("✗ Error de conexión: " + e.getMessage());
        } finally {
            disconnect();
            scanner.close();
        }
    }

    private void sendMessages(Scanner scanner) {
        try {
            while (connected) {
                System.out.print(username + "> ");
                String message = scanner.nextLine().trim();

                if (message.equalsIgnoreCase("salir")) {
                    connected = false;
                    break;
                }

                if (!message.isEmpty()) {
                    writer.println(message);
                }
            }
        } catch (Exception e) {
            System.out.println("✗ Error al enviar: " + e.getMessage());
        }
    }

    private void receiveMessages() {
        try {
            String message;
            while ((message = reader.readLine()) != null && connected) {
                System.out.println("[" + getTime() + "] " + message);
            }
        } catch (IOException e) {
            if (connected) {
                System.out.println("\n✗ Desconectado del servidor");
            }
        }
    }

    private void disconnect() {
        try {
            connected = false;
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            System.out.println("\n✓ Desconectado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getTime() {
        return LocalDateTime.now().format(formatter);
    }

    public static void main(String[] args) {
        ClientCLI client = new ClientCLI();
        client.connect();
    }
}
