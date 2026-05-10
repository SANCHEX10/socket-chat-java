package cli;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor TCP para chat con interfaz CLI
 * Muestra timestamps y lista de clientes conectados
 */
public class ServerCLI {
    private ServerSocket serverSocket;
    private final Set<ClientHandler> clients = ConcurrentHashMap.newKeySet();
    private final int port;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    public ServerCLI(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        printHeader();
        System.out.println("✓ Servidor iniciado en puerto " + port);
        System.out.println("Esperando conexiones...\n");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket, this);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    private void printHeader() {
        System.out.println("╔════════════════════════════════════╗");
        System.out.println("║   SERVIDOR DE CHAT - CLI MODE      ║");
        System.out.println("╚════════════════════════════════════╝\n");
    }

    public void broadcast(String message) {
        System.out.println("[" + getTime() + "] " + message);
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public int getClientCount() {
        return clients.size();
    }

    private String getTime() {
        return LocalDateTime.now().format(formatter);
    }

    public static class ClientHandler implements Runnable {
        private final Socket socket;
        private final PrintWriter writer;
        private final BufferedReader reader;
        private final String username;
        private final ServerCLI server;

        public ClientHandler(Socket socket, ServerCLI server) throws IOException {
            this.socket = socket;
            this.server = server;
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.username = reader.readLine();
        }

        @Override
        public void run() {
            try {
                server.broadcast("✓ " + username + " conectado (Total: " + server.getClientCount() + ")");
                String message;
                while ((message = reader.readLine()) != null) {
                    server.broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                // Conexión cerrada
            } finally {
                server.removeClient(this);
                server.broadcast("✗ " + username + " desconectado (Total: " + server.getClientCount() + ")");
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

    public static void main(String[] args) throws IOException {
        ServerCLI server = new ServerCLI(5555);
        server.start();
    }
}
