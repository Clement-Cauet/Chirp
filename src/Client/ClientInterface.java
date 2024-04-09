package Client;

import Server.DatabaseAccess;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientInterface extends JFrame {

    private static final int PORT = 8964;

    private JPanel loginPanel, chatPanel;
    private JTextArea pseudoArea;
    private JEditorPane chatArea;
    private JTextField pseudoField, messageField;
    private JPasswordField passwordField;
    private JButton sendButton;
    private JList<String> roomList;
    private DefaultListModel<String> roomListModel;
    private JTextArea connectedUsersArea;


    private int id;
    private String pseudo, password;
    private PrintWriter out = new PrintWriter(System.out); ;

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

        connectToServer();
    }

    private void connectToServer() {
        new Thread(() -> {
            while (true) {
                try {
                    Socket socket = new Socket("127.0.0.1", PORT);
                    out = new PrintWriter(socket.getOutputStream(), true);
                    new Thread(new ServerHandler(socket)).start();
                    break;
                } catch (IOException e) {
                    chatArea.setText("Unable to connect to the server. Retrying in 15 seconds..." + System.lineSeparator());
                    try {
                        Thread.sleep(15000);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }).start();
    }


    // Creation the chat panel interface
    private void displayChatPanel() {
        chatArea = new JEditorPane();
        chatArea.setEditable(false);
        chatArea.setContentType("text/html"); // Set content type to HTML
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

        // Panel to display connected users
        JPanel connectedUsersPanel = new JPanel(new BorderLayout());
        connectedUsersPanel.setPreferredSize(new Dimension(200, 600)); // Adjust size as needed
        connectedUsersPanel.setBackground(Color.LIGHT_GRAY); // Adjust background color as needed
        connectedUsersPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Adjust border as needed

        connectedUsersArea = new JTextArea();
        connectedUsersArea.setEditable(false);
        connectedUsersPanel.add(new JScrollPane(connectedUsersArea), BorderLayout.CENTER);

        chatPanel = new JPanel();
        chatPanel.setLayout(new BorderLayout());
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(messagePanel, BorderLayout.SOUTH);
        chatPanel.add(roomPanel, BorderLayout.WEST);
        chatPanel.add(connectedUsersPanel, BorderLayout.EAST); // Add the panel to display connected users

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
                out.flush();
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
        if (out != null && !out.checkError()) {
            out.println("Message;" + id + ";" + pseudo + ";" + id_room + ";" + text);
            String message = pseudo + ": \n" + text + "\n\n";
            appendTextToChatArea(message); // Append the new message to the chat area
        } else {
            appendTextToChatArea("Le serveur est pas disponible" + System.lineSeparator());
        }
    }

    private void appendTextToChatArea(String text) {
        Document doc = chatArea.getDocument();
        try {
            doc.insertString(doc.getLength(), text, null);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    // Display message in chat
    private void displayMessage(int id_room) {
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
                            chatArea.setText(values[2] + System.lineSeparator());
                    } else if (values[0].equals("Load")) {
                        for (int i = 1; i < values.length; i++) {
                            appendTextToChatArea(values[i] + "<br>");
                        }
                    } else if (values[0].equals("Room")) {
                        System.out.println("Room receive");
                        roomListModel.addElement(values[1]);
                        refreshInterface();
                    } else if (values[0].equals("REFRESH")) { // Refresh interface
                        refreshInterface();
                    } else if (values[0].equals("ConnectedUsers")) {
                        System.out.println("T co");
                        connectedUsersArea.append("User connected :\n");
                        connectedUsersArea.append(values[1]);
                    } else if (values[0].equals("ResetUser")) {
                        System.out.println("Reset call");
                        connectedUsersArea.setText("");
                    } else if (values[0].equals("Sauce")) {
                        System.out.println("Test ok");

                        String code = values[1];
                        String title = values[2];
                        String language = values[3];
                        String page = values[4];

                        //https://s9.3hentai.xyz/d240942/2.jpg
                        String imageUrl = "https://s9.3hentai.xyz/d" + code + "/1.jpg";
                        System.out.println(imageUrl);

                        // Format the image URL as an HTML image tag
                        String htmlImage = "<img src='" + imageUrl + "' width='180' height='220'>";

                        chatArea.setText("");
                        // Append the HTML image tag to the JTextArea using HTML formatting
                        chatArea.setText("<html><body>"
                                + "ID: " + id + "<br>"
                                + "Title: " + title + "<br>"
                                + "Language: " + language + "<br>"
                                + htmlImage
                                + "</body></html>");
                    }
                }
            } catch (IOException e) {
                chatArea.setText("Le serveur s'est déconnecté.\n");
            }
        }

        // Refreshes the interface
        private void refreshInterface() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    revalidate();
                    repaint();
                }
            });
        }
    }
}