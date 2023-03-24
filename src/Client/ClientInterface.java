package Client;

import Server.DatabaseAccess;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientInterface extends JFrame {

    private static final int PORT = 8964;

    private JPanel loginPanel, chatPanel;
    private JTextArea pseudoArea, chatArea;
    private JTextField pseudoField, messageField;
    private JPasswordField passwordField;
    private JButton sendButton;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;

    private int id;
    private String pseudo, password;
    private PrintWriter out;

    public static void main(String[] args) throws Exception {
        new ClientInterface();
    }

    // Constructor ClientInterface
    public ClientInterface() {
        super("Chirp");

        displayLoginPanel();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setVisible(true);

        try {
            Socket socket = new Socket("10.200.0.153", PORT);
            out = new PrintWriter(socket.getOutputStream(), true);

            new Thread(new ServerHandler(socket)).start();
        } catch (IOException e) {
            chatArea.append("Unable to connect to the server.\n");
        }

    }

    // Creation the chat panel interface
    private void displayChatPanel() {
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);
        chatArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {

            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        pseudoArea = new JTextArea();
        pseudoArea.setText(pseudo);

        messageField = new JTextField(40);
        messageField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendMessage(roomList.getSelectedIndex() + 1, messageField.getText());
            }
        });

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                sendMessage(roomList.getSelectedIndex() + 1, messageField.getText());
            }
        });

        JPanel messagePanel = new JPanel();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.add(pseudoArea, BorderLayout.WEST);
        messagePanel.add(messageField, BorderLayout.CENTER);
        messagePanel.add(sendButton, BorderLayout.EAST);

        roomListModel = new DefaultListModel<>();
        roomList = new JList<>(roomListModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomList.setSelectedIndex(0);
        roomList.setVisibleRowCount(5);
        roomList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                displayMessage(roomList.getSelectedIndex() + 1);
            }
        });
        JButton createButton = new JButton("Add room");
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String roomName = JOptionPane.showInputDialog("Name room :");
                if (roomName != null && !roomName.isEmpty()) {
                    roomListModel.addElement(roomName);
                    int index = roomListModel.getSize() - 1;
                    roomList.ensureIndexIsVisible(index);
                    roomList.setSelectedIndex(index);

                    out.println("Room;2;" + roomName);
                }
            }
        });
        JScrollPane roomScrollPane = new JScrollPane(roomList);
        JPanel roomPanel = new JPanel(new BorderLayout());
        roomPanel.add(createButton, BorderLayout.NORTH);
        roomPanel.add(roomScrollPane, BorderLayout.CENTER);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        chatPanel.add(roomPanel, BorderLayout.WEST);

        add(chatPanel, BorderLayout.CENTER);
        loginPanel.setVisible(false);

        displayRoom();
        displayMessage(roomList.getSelectedIndex() + 1);

    }

    // Creation the login panel interface
    private void displayLoginPanel() {
        JLabel pseudoLabel = new JLabel("Username:");
        JLabel passwordLabel = new JLabel("Password:");

        pseudoField = new JTextField(15);
        passwordField = new JPasswordField(15);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                out.println("Login;" + pseudoField.getText() + ";" + passwordField.getText());
            }
        });

        JButton inscriptionButton = new JButton("Sign in");
        inscriptionButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                out.println("Sign;" + pseudoField.getText() + ";" + passwordField.getText());
            }
        });

        loginPanel = new JPanel();
        loginPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
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
        loginPanel.add(loginButton, gbc);

        gbc.gridx = 1;
        loginPanel.add(inscriptionButton, gbc);

        add(loginPanel, BorderLayout.CENTER);
    }

    // Send message to the server
    private void sendMessage(int id_room, String text) {
        out.println("Message;" + id + ";" + pseudo + ";" + id_room + ";" + text);
        chatArea.append(pseudo + ": \n" + text + "\n\n");
        messageField.setText("");
    }

    // Display message in chat
    private void displayMessage(int id_room) {
        chatArea.setText("");
        out.println("Load;" + id_room);
    }

    // Display all room
    private void displayRoom() {
        out.println("Room;1");
    }

    // Message receive by server
    private class ServerHandler implements Runnable {
        private final Socket socket;

        public ServerHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                while (true) {
                    DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                    String inputLine = inputStream.readUTF();
                    System.out.println("Received message from server: " + inputLine);

                    String[] values = inputLine.split(";");

                    if (values[0].equals("Login")) { // Login
                        if (values[1].equals("true")) {
                            id = Integer.valueOf(values[2]);
                            pseudo = values[3];
                            displayChatPanel();
                        }
                    } else if (values[0].equals("Sign")) {
                        if (values[1].equals("true")) {
                            id = Integer.valueOf(values[2]);
                            pseudo = values[3];
                            displayChatPanel();
                        }
                    } else if (values[0].equals("Message")) {
                        int id_room = roomList.getSelectedIndex() + 1;
                        if (Integer.valueOf(values[1]).equals(id_room))
                            chatArea.append(values[2]);
                    } else if (values[0].equals("Load")) {
                        for (int i = 1; i < values.length; i++) {
                            chatArea.append(values[i]);
                        }
                    } else if (values[0].equals("Room")) {
                        roomListModel.addElement(values[1]);
                    }
                }
            } catch (IOException e) {
                chatArea.append("Le serveur s'est déconnecté.\n");
            }
        }
    }

}