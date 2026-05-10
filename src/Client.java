import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Cliente TCP básico para chat con sockets
 */
public class Client {
    private String host;
    private int port;
    private String username;

    public Client(String host, int port, String username) {
        this.host = host;
        this.port = port;
        this.username = username;
    }

    public void connect() throws IOException {
        Socket socket = new Socket(host, port);
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

        writer.println(username);

        new Thread(() -> receiveMessages(reader)).start();
        sendMessages(writer);

        socket.close();
    }

    private void receiveMessages(BufferedReader reader) {
        try {
            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(message);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendMessages(PrintWriter writer) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String message = scanner.nextLine();
            writer.println(message);
        }
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client("localhost", 5555, "Usuario1");
        client.connect();
    }
}
