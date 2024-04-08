package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Logger;

public class ServerHandler implements Runnable {

    private final Socket clientSocket;
    private final DatabaseAccess databaseAccess;
    private Logger logger;

    private String username;

    // Constructor
    public ServerHandler(Socket clientSocket, DatabaseAccess databaseAccess, Logger logger) {
        this.clientSocket = clientSocket;
        this.databaseAccess = databaseAccess;
        this.logger = logger;
    }

    @Override
    public void run() {
        try {

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null) {

                String[] values = inputLine.split(";");

                if (values[0].equals("Login")) {

                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    ArrayList userLogin = databaseAccess.userLogin(values[1], values[2]);

                    if (userLogin.size() != 0) {
                        username = String.valueOf(userLogin.get(1));

                        logger.info("Login user:" + userLogin.get(0));

                        outputStream.writeUTF("Login;true;" + userLogin.get(0) + ";" + userLogin.get(1));
                        outputStream.flush();

                        outputStream.writeUTF("ResetUser;");
                        outputStream.flush();

                        sendConnectedUsersToAll(username);
                    } else {
                        outputStream.writeUTF("Login;false");
                        outputStream.flush();
                    }

                } else if (values[0].equals("Sign")) {
                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                    databaseAccess.userInscription(values[1], values[2]);

                    ArrayList userLogin = databaseAccess.userLogin(values[1], values[2]);

                    try {
                        logger.info("Sign in user:" + userLogin.get(0));
                        outputStream.writeUTF("Sign;true;" + userLogin.get(0) + ";" + userLogin.get(1));
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (values[0].equals("Message")) {

                    databaseAccess.addChatMessage(Integer.valueOf(values[1]), Integer.valueOf(values[3]), values[4]);

                    logger.info("User:" + values[1] + ";Room:" + values[3] + ";Message:" + values[4]);

                    for (Socket clientSocket : ServerFunction.clients) {
                        if (!clientSocket.isClosed() && clientSocket != this.clientSocket) {
                            try {
                                DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                                outputStream.writeUTF("Message;" + values[3] + ";" + values[2] + ": \n" + values[4] + "\n\n");
                                outputStream.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                } else if (values[0].equals("Load")) {

                    try {
                        DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                        ArrayList chatHistory = databaseAccess.getChatHistory(Integer.valueOf(values[1]));

                        String out = "Load;";
                        for (int i = 0; i < chatHistory.size(); i++) {
                            out = out + chatHistory.get(i);
                        }
                        outputStream.writeUTF(out);
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else if (values[0].equals("Room")) {

                    if (Integer.valueOf(values[1]).equals(1)) {

                        try {
                            DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                            ArrayList room = databaseAccess.getRoom();

                            for (int i = 0; i < room.size(); i++) {
                                outputStream.writeUTF("Room;" + room.get(i));
                                outputStream.flush();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    } else {

                        try {
                            databaseAccess.addRoom(values[2]);

                            logger.info("Add Room :" + values[2]);

                            for (Socket clientSocket : ServerFunction.clients) {
                                if (clientSocket != this.clientSocket) {
                                    DataOutputStream outputStream = new DataOutputStream(clientSocket.getOutputStream());
                                    outputStream.writeUTF("Room;" + values[2]);
                                    outputStream.flush();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if (values[0].equals("/sauce")) {

                    

                }
            }

        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
            logger.severe("Error handling client: " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
                logger.warning("Client logout");


            } catch (IOException e) {
                System.err.println("Error closing client socket: " + e.getMessage());
                logger.severe("Error closing client socket: " + e.getMessage());
            }
        }
    }

    private void sendConnectedUsersToAll(String username) {
        ArrayList<String> connectedUsers = new ArrayList<>();

        // Add the username to the list of connected users
        connectedUsers.add(username);

        for (Socket socket : ServerFunction.clients) {
            if (!socket.isClosed()) {
                try {
                    DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    outputStream.writeUTF("ConnectedUsers;" + String.join(",", connectedUsers));
                    outputStream.flush();
                } catch (IOException e) {
                    System.err.println("Error sending connected users: " + e.getMessage());
                    logger.severe("Error sending connected users: " + e.getMessage());
                }
            }
        }
    }
}