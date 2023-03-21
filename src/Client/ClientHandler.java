package Client;

import Server.DatabaseAccess;
import Server.ServerFunction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private final DatabaseAccess databaseAccess;
    private String username;

    private int id_user = 1, id_room = 1;

    public ClientHandler(Socket clientSocket, DatabaseAccess databaseAccess) {
        this.clientSocket = clientSocket;
        this.databaseAccess = databaseAccess;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Le serveur a reçu le message : " + inputLine);
                databaseAccess.addChatMessage(id_user, id_room, inputLine);
                // Envoyer le message reçu à tous les clients connectés
                for (Socket client : ClientHandlerList.getClients()) {
                    if (client != clientSocket) {
                        PrintWriter printWriter = new PrintWriter(client.getOutputStream(), true);
                        printWriter.println(inputLine);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private static class ClientHandlerList {
        private static final java.util.List<Socket> clients = new java.util.ArrayList<>();

        public static synchronized void addClient(Socket socket) {
            clients.add(socket);
        }

        public static synchronized void removeClient(Socket socket) {
            clients.remove(socket);
        }

        public static synchronized java.util.List<Socket> getClients() {
            return clients;
        }
    }
}