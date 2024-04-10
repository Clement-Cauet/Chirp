package Server;

import java.sql.*;
import java.util.ArrayList;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class DatabaseAccess {
    private static String URL = "jdbc:mysql://localhost:3306/chirp?verifyServerCertificate=false&useSSL=true", USERNAME = "root", PASSWORD = "";

    private Connection connection;

    // Constructor
    public DatabaseAccess() throws Exception {
        if (connection == null)
            connection = getDatabaseConnection();
    }

    // Connexion database
    public static Connection getDatabaseConnection() throws Exception {
        Connection connection = null;
        boolean hasFailed = false;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
        }
        catch (SQLException se) { hasFailed = true; se.printStackTrace(); }
        catch (Exception e) { hasFailed = true; e.printStackTrace(); }
        finally {
            try {
                if (hasFailed && connection != null) {
                    connection.close();
                    connection = null;
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        if(connection == null) {  throw new Exception("Connection is null"); }
        return connection;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = md.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to ash", e);
        }
    }

    // Login user
    public ArrayList userLogin(String pseudo, String password) {
        ArrayList user = new ArrayList<>();
        String hashedPassword = hashPassword(password);
        try {
            Statement statement = connection.createStatement();
            String request = "SELECT * FROM user WHERE user.pseudo = '" + pseudo + "' AND user.password = '" + hashedPassword + "'";
            ResultSet resultSet = statement.executeQuery(request);

            if (resultSet.next()) {
                user.add(resultSet.getInt("id"));
                user.add(resultSet.getString("pseudo"));
                user.add(resultSet.getString("password"));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("User connection");
        return user;
    }

    // Inscription user
    public void userInscription(String pseudo, String password) {
        try {
            String hashedPassword = hashPassword(password);
            String request = "INSERT INTO `user`(`pseudo`, `password`) VALUES (?, ?)";
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setString(1, pseudo);
            statement.setString(2, hashedPassword);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Add message in database
    public void addChatMessage(int id_user, int id_room, String text) {
        try {
            String request = "INSERT INTO `message`(`id_user`, `id_room`, `text`) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setInt(1, id_user);
            statement.setInt(2, id_room);
            statement.setString(3, text);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Receive all message by room in database
    public ArrayList getChatHistory(int id_room) {
        ArrayList<String> history = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String request = "SELECT user.pseudo, message.text FROM message INNER JOIN user ON message.id_user = user.id WHERE message.id_room = " + id_room;
            ResultSet resultSet = statement.executeQuery(request);
            System.out.println(id_room);
            while (resultSet.next()) {
                String pseudo = resultSet.getString("pseudo");
                String text = resultSet.getString("text");
                history.add(pseudo + ": \n" + text + "\n\n");
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return history;
    }

    // Add room in database
    public void addRoom(String name) {
        try {
            String request = "INSERT INTO `room`(`name`) VALUES (?)";
            PreparedStatement statement = connection.prepareStatement(request);
            statement.setString(1, name);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    // Receive all room
    public ArrayList getRoom() {
        ArrayList<String> room = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String request = "SELECT name FROM `room` ORDER BY id ASC";
            ResultSet resultSet = statement.executeQuery(request);

            while (resultSet.next()) {
                String name = resultSet.getString("name");
                room.add(name);
            }

        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return room;
    }

}
