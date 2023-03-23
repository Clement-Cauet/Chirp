package Server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerFunction {

    private static final int PORT = 8964;
    private static DatabaseAccess databaseAccess;
    private static Socket clientSocket;
    public static ArrayList<Socket> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerFunction();
    }

    public static void ServerFunction() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1234);
        DatabaseAccess databaseAccess = new DatabaseAccess();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ServerHandler clientHandler = new ServerHandler(clientSocket, databaseAccess);
            clients.add(clientSocket);
            new Thread(clientHandler).start();
        }
    }

}