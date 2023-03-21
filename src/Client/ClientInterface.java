package Client;

import Server.DatabaseAccess;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientInterface extends JFrame {

    private DatabaseAccess databaseAccess;

    private JPanel loginPanel, chatPanel;
    private JTextArea chatArea;
    private JTextField pseudoField, messageField;
    private JPasswordField passwordField;
    private JButton sendButton;
    private JList<String> roomList;

    private int id;
    private String pseudo, password;
    private PrintWriter out;

    public static void main(String[] args) throws Exception {
        new ClientInterface();
    }

    public ClientInterface() throws Exception {
        super("Chirp");

        // Créer le panneau de texte pour le chat
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // Créer le champ de texte pour les messages et le bouton send
        messageField = new JTextField(40);
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                out.println(messageField.getText());
                messageField.setText("");
            }
        });
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                out.println(messageField.getText());
                messageField.setText("");
            }
        });
        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        // Créer la liste de salons
        String[] roomNames = {"General", "Politics", "Sports", "Movies", "Music"};
        roomList = new JList<String>(roomNames);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setSelectedIndex(0);
        roomList.setVisibleRowCount(5);
        roomList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {

            }
        });
        JScrollPane roomScrollPane = new JScrollPane(roomList);

        // Créer le formulaire de connexion
        JLabel pseudoLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");
        pseudoField = new JTextField(15);
        passwordField = new JPasswordField(15);
        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                /*try {
                    if (databaseAccess.getDatabaseConnection() != null) {
                        ArrayList userLogin = databaseAccess.userLogin(pseudoField.getText(), new String(passwordField.getPassword()));

                        if (!userLogin.isEmpty()) {

                            id = (int) userLogin.get(0);
                            pseudo = (String) userLogin.get(1);
                            password = (String) userLogin.get(2);*/

                            // Créer le panneau de chat et cacher le panneau de connexion
                            chatPanel = new JPanel();
                            chatPanel.setLayout(new BorderLayout());
                            chatPanel.add(chatScrollPane, BorderLayout.CENTER);
                            chatPanel.add(messagePanel, BorderLayout.SOUTH);
                            chatPanel.add(roomScrollPane, BorderLayout.WEST);
                            add(chatPanel, BorderLayout.CENTER);
                            loginPanel.setVisible(false);

                        /*} else
                            JOptionPane.showMessageDialog(ClientInterface.this, "Invalid username or password");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }*/
            }
        });
        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(5, 5, 5, 5);
        loginPanel.add(pseudoLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(pseudoField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        loginPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        loginPanel.add(passwordField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginPanel.add(loginButton, gbc);

        // Ajouter le panneau de connexion à la fenêtre
        add(loginPanel, BorderLayout.CENTER);

        // Configurer la fenêtre
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);

        // Connexion au serveur
        try {
            Socket socket = new Socket("localhost", 1234);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Lecture des messages du serveur
            new Thread(new ServerHandler(socket)).start();
        } catch (IOException e) {
            chatArea.append("Impossible de se connecter au serveur.\n");
        }

    }

    private class ServerHandler implements Runnable {
        private final Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                while (true) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out.println(in.readLine());
                    String message = new java.util.Scanner(socket.getInputStream()).nextLine();
                    chatArea.append(message + "\n");
                }
            } catch (IOException e) {
                chatArea.append("Le serveur s'est déconnecté.\n");
            }
        }
    }

}