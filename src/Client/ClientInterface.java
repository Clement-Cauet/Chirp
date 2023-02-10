package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientInterface {

    private static ClientFunction client;

    public static void main(String[] args) throws IOException {

        client = new ClientFunction();

        JFrame frame = new JFrame("Chirp");
        Container content = frame.getContentPane();
        content.setLayout(new FlowLayout());

        JButton button = new JButton("Send");
        content.add(button);

        JTextField textField = new JTextField(20);
        content.add(textField);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    client.send(textField.getText());
                    System.out.println("Send message");
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 100);
        frame.setVisible(true);
    }
}