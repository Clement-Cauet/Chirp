package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
/*
 * www.codeurjava.com
 */
public class ClientFunction {

    private static Socket clientSocket;

    public ClientFunction() throws IOException {
        clientSocket = new Socket("127.0.0.1",5000);
    }

    public static void main(String[] args) {

        final BufferedReader in;
        final Scanner sc = new Scanner(System.in);//pour lire à partir du clavier

        try {

            //flux pour recevoir
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            new ClientInterface();

            Thread recevoir = new Thread(new Runnable() {
                String msg;
                @Override
                public void run() {
                    try {
                        msg = in.readLine();
                        while(msg!=null){
                            System.out.println("Server.Serveur : "+msg);
                            msg = in.readLine();
                        }
                        System.out.println("Server.Serveur déconecté");
                        //out.close();
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            recevoir.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void send(String message) throws IOException {
        //flux pour envoyer
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream());
        Thread envoyer = new Thread(new Runnable() {
            @Override
            public void run() {
                out.println(message);
                out.flush();
            }
        });
        envoyer.start();
    }
}