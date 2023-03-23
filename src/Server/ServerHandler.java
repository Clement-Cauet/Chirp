package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ServerHandler implements Runnable {

    private final Socket clientSocket;
    private final DatabaseAccess databaseAccess;
    private String username;

    public ServerHandler(Socket clientSocket, DatabaseAccess databaseAccess) {
        this.clientSocket = clientSocket;
        this.databaseAccess = databaseAccess;
    }

    @Override
    public void run() {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;
            while ((inputLine = bufferedReader.readLine()) != null) {
                System.out.println("Le serveur a reçu le message : " + inputLine);
                String[] values = inputLine.split(";");

                if (values[0].equals("Auth")) {

                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    ArrayList userLogin = databaseAccess.userLogin(values[1], values[2]);

                    if (userLogin.size() != 0) {
                        outputStream.writeUTF("Auth;true;" + userLogin.get(0) + ";" + userLogin.get(1));
                        outputStream.flush();
                    } else {
                        outputStream.writeUTF("Auth;false");
                        outputStream.flush();
                    }

                } else if (values[0].equals("Sign")) {

                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    databaseAccess.userInscription(values[1], values[2]);
                    ArrayList userLogin = databaseAccess.userLogin(values[1], values[2]);

                    outputStream.writeUTF("Sign;true;" + userLogin.get(0) + ";" + userLogin.get(1));
                    outputStream.flush();

                } else if (values[0].equals("Message")) {

                    databaseAccess.addChatMessage(Integer.valueOf(values[1]), Integer.valueOf(values[3]), values[4]);

                    for (Socket clientSocket : ServerFunction.clients) {
                        if (clientSocket != this.clientSocket) {
                            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                            outputStream.writeUTF("Message;" + values[3] + ";" + values[2] + ": \n" + values[4] + "\n\n");
                            outputStream.flush();
                        }
                    }

                } else if (values[0].equals("Load")) {

                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    ArrayList chatHistory = databaseAccess.getChatHistory(Integer.valueOf(values[1]));

                    String out = "Load;";
                    for (int i = 0; i < chatHistory.size(); i++) {
                        out = out + chatHistory.get(i);
                    }
                    outputStream.writeUTF(out);
                    outputStream.flush();

                } else if (values[0].equals("Room")) {

                    if (Integer.valueOf(values[1]).equals(1)) {

                        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                        ArrayList room = databaseAccess.getRoom();

                        for (int i = 0; i < room.size(); i++) {
                            outputStream.writeUTF("Room;" + room.get(i));
                            outputStream.flush();
                        }

                    } else {

                        databaseAccess.addRoom(values[2]);

                        for (Socket clientSocket : ServerFunction.clients) {
                            if (clientSocket != this.clientSocket) {
                                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                                outputStream.writeUTF("Room;" + values[2]);
                                outputStream.flush();
                            }
                        }
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
}