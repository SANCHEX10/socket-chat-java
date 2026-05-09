import java.io.*;
import java.net.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Servidor TCP para chat con interfaz CLI
 * Muestra timestamps y lista de clientes conectados
 */
public class ServerCLI {
    private ServerSocket serverSocket;
    private Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private int port;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

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
        private ServerCLI server;

        public ClientHandler(Socket socket, ServerCLI server) throws IOException {
            this.socket = socket;
            this.server = server;
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = reader.readLine();
        }

        @Override
        public void run() {
            try {
                server.broadcast("✓ " + username + " conectado (Total: " + server.clients.size() + ")");
                String message;
                while ((message = reader.readLine()) != null) {
                    server.broadcast(username + ": " + message);
                }
            } catch (IOException e) {
                // Conexión cerrada
            } finally {
                server.broadcast("✗ " + username + " desconectado (Total: " + server.clients.size() + ")");
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

    public static void main(String[] args) throws IOException {
        ServerCLI server = new ServerCLI(5555);
        server.start();
    }
}
