package Server;

import Client.ClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class ServerFunction {

    private static final int PORT = 8964;
    private static DatabaseAccess databaseAccess;
    private static Socket clientSocket;
    public static ArrayList<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerFunction();
    }

    public static void ServerFunction() throws Exception {
        ServerSocket serverSocket = new ServerSocket(1234);
        DatabaseAccess databaseAccess = new DatabaseAccess();

        while (true) {
            Socket clientSocket = serverSocket.accept();
            new Thread(new ClientHandler(clientSocket, databaseAccess)).start();
        }
    }

}