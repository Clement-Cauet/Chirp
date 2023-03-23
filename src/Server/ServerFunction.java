package Server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerFunction {

    private static final int PORT = 8964;
    private static DatabaseAccess databaseAccess;
    private static Socket clientSocket;
    public static ArrayList<Socket> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerFunction();
    }

    public static void ServerFunction() throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        DatabaseAccess databaseAccess = new DatabaseAccess();

        String logsDir = "logs";
        File logsFolder = new File(logsDir);
        if (!logsFolder.exists()) {
            logsFolder.mkdirs();
        }

        String logFilePath = logsDir + "/log.log";
        File logFile = new File(logFilePath);
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Logger logger = Logger.getLogger("MyLog");
        FileHandler fileHandler = new FileHandler("logs/log.log");
        SimpleFormatter formatter = new SimpleFormatter();
        logger.addHandler(fileHandler);
        fileHandler.setFormatter(formatter);

        logger.warning("Server start");

        while (true) {
            Socket clientSocket = serverSocket.accept();
            ServerHandler clientHandler = new ServerHandler(clientSocket, databaseAccess, logger);
            clients.add(clientSocket);
            logger.warning("new Client");
            new Thread(clientHandler).start();
        }
    }

}