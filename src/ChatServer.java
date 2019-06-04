import transmissionControl.Connection;
import transmissionControl.ConnectionListener;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class ChatServer implements ConnectionListener {

    private final ArrayList<Connection> connections = new ArrayList<>();

    private ChatServer(int port) throws IOException{
        Logger.getLogger(ChatServer.class).info("Server is running...");
        ServerSocket serverSocket = new ServerSocket(port);
        while (true){
            try{
                new Connection(this, serverSocket.accept());
            } catch(IOException e){
                Logger.getLogger(ChatServer.class).error("Connection exception: " + e);
            }
        }
    }

    @Override
    public synchronized void connect(Connection connection) {
        connections.add(connection);
        Logger.getLogger(ChatServer.class).info("Client connected: " + connection);
        sendToAll("Client connected: " + connection);
    }

    @Override
    public synchronized void recv(Connection connectionFrom, String value) {
        System.out.println(connectionFrom + "  " + value);
        sendToAll(value);
    }

    private void sendToAll(String value){
        for (Connection connection : connections){
            connection.sendMsg(value);
        }
    }

    @Override
    public synchronized void shutdown(Connection connection) {
        connections.remove(connection);
        Logger.getLogger(ChatServer.class).info("Client disconnected: " + connection);
        sendToAll("Client disconnected: " + connection);
    }

    @Override
    public synchronized void onException(Connection connection, Exception e) {
        Logger.getLogger(ChatServer.class).error("Connection exception: " + e);
    }

    public static void main(String[] args){
        int port;
        Scanner in = new Scanner(System.in);
        System.out.print("Enter port number from 1 to 65355 to set server: ");
        try {
            port = in.nextInt();
            String IP_addr = "127.0.0.1";
            if (port < 1 || port > 65535) throw new InputMismatchException("Wrong port number");
            IP_addr = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server IP_addr: " + IP_addr);
            new ChatServer(port);
        } catch(UnknownHostException ex) {
            Logger.getLogger(ChatServer.class).error(ex);
        } catch (IOException e){
            Logger.getLogger(ChatServer.class).error(e);
            ChatServer.main(args);
        }
        in.close();
    }
}

