import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Servidor TCP para chat con sockets
 * Acepta conexiones de múltiples clientes y retransmite mensajes
 */
public class Server {
    private ServerSocket serverSocket;
    private Set<ClientHandler> clients = Collections.synchronizedSet(new HashSet<>());
    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        serverSocket = new ServerSocket(port);
        System.out.println("✓ Servidor iniciado en puerto " + port);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(clientSocket, this);
            clients.add(handler);
            new Thread(handler).start();
        }
    }

    public void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public void removeClient(ClientHandler client) {
        clients.remove(client);
    }

    public static void main(String[] args) throws IOException {
        Server server = new Server(5555);
        server.start();
    }

    /**
     * Maneja la comunicación con un cliente individual
     */
    public static class ClientHandler implements Runnable {
        private Socket socket;
        private PrintWriter writer;
        private BufferedReader reader;
        private String username;
        private Server server;

        public ClientHandler(Socket socket, Server server) throws IOException {
            this.socket = socket;
            this.server = server;
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.username = reader.readLine();
        }

        @Override
        public void run() {
            try {
                server.broadcast("[SERVIDOR] " + username + " se conectó", this);
                String message;
                while ((message = reader.readLine()) != null) {
                    server.broadcast(username + ": " + message, this);
                }
            } catch (IOException e) {
                // Conexión cerrada
            } finally {
                try {
                    server.broadcast("[SERVIDOR] " + username + " se desconectó", this);
                    server.removeClient(this);
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
}
