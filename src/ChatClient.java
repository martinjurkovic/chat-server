import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.simple.JSONObject;

public class ChatClient extends Thread {
    protected int serverPort = 8888;

    public static void main(String[] args) throws Exception {
        new ChatClient();
    }

    public ChatClient() throws Exception {
        Socket socket = null;
        DataInputStream in = null;
        DataOutputStream out = null;

        BufferedReader std_in = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Vpisi svoje ime: ");
        String ime = std_in.readLine();

        // connect to the chat server
        try {
            System.out.println("[system] connecting to chat server ...");
            socket = new Socket("localhost", serverPort); // create socket connection
            in = new DataInputStream(socket.getInputStream()); // create input stream for listening for incoming messages
            out = new DataOutputStream(socket.getOutputStream()); // create output stream for sending messages
            System.out.println("[system] connected");

            // posljimo pozdravno sporocilo

            JSONObject sporocilo1 = new JSONObject();
            Date date = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            sporocilo1.put("date", formatter.format(date));
            sporocilo1.put("type", "login");
            sporocilo1.put("source", ime);
            sporocilo1.put("dest", "login");
            sporocilo1.put("message", "Povezal se je uporabnik " + ime);
            this.sendMessage(sporocilo1.toString(), out);


            ChatClientMessageReceiver message_receiver = new ChatClientMessageReceiver(in); // create a separate thread for listening to messages from the chat server
            message_receiver.start(); // run the new thread
        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
        }

        // read from STDIN and send messages to the chat server

        String userInput;

        //System.out.println("Vpisi ukaz: ");
        while ((userInput = std_in.readLine()) != null) { // read a line from the console
            JSONObject sporocilo = new JSONObject();

            if (userInput.equals("/public")) {
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                sporocilo.put("date", formatter.format(date));

                sporocilo.put("type", "public");
                sporocilo.put("source", ime);
                sporocilo.put("dest", "broadcastFF");
                System.out.println("Vpisi sporocilo: ");
                userInput = std_in.readLine();
                sporocilo.put("message", userInput);

                this.sendMessage(sporocilo.toString(), out); // send the message to the chat server
            } else if (userInput.equals("/private")) {
                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                sporocilo.put("date", formatter.format(date));

                System.out.println("Vpisi ime prejemnika: ");
                userInput = std_in.readLine();
                String prejemnik = userInput;
                sporocilo.put("type", "private");
                sporocilo.put("source", ime);
                sporocilo.put("dest", prejemnik);
                System.out.println("Vpisi sporocilo: ");
                userInput = std_in.readLine();
                sporocilo.put("message", userInput);
                this.sendMessage(sporocilo.toString(), out); // send the message to the chat server

            } else if (userInput.equals("/help")) {
                System.out.printf("Ukazi delujejo tako, da najprej vpises ukaz, pritisnes enter ([enter]), nato pa vpises potrebne parametre (npr.: \"message\") ter [enter]\n");
                System.out.printf("Seznam ukazov:\n");
                System.out.printf("/public: [enter]\n");
                System.out.printf("      \"message\" [enter]\n");
                System.out.printf("/private: [enter]\n");
                System.out.printf("      \"destination username\" [enter]\n");
                System.out.printf("      \"message\" [enter]\n");
            } /*else if (userInput.equals("/exit")) {

                Date date = new Date();
                SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                sporocilo.put("date", formatter.format(date));

                sporocilo.put("type", "exit");
                sporocilo.put("source", ime);
                sporocilo.put("dest", "broadcastFF");
                sporocilo.put("message", "Odšel je uporabnik: " + ime);

                this.sendMessage(sporocilo.toString(), out); // send the message to the chat server

            } */else {
                System.out.println("NEZNAN UKAZ! Vpiši /help za pomoč!");
            }
        }

        // cleanup
        out.close();
        in.close();
        std_in.close();
        socket.close();
    }

    private void sendMessage(String message, DataOutputStream out) {
        try {
            out.writeUTF(message); // send the message to the chat server
            out.flush(); // ensure the message has been sent
        } catch (IOException e) {
            System.err.println("[system] could not send message");
            e.printStackTrace(System.err);
        }
    }
}

// wait for messages from the chat server and print the out
class ChatClientMessageReceiver extends Thread {
    private DataInputStream in;

    public ChatClientMessageReceiver(DataInputStream in) {
        this.in = in;
    }

    public void run() {
        try {
            String message;
            while ((message = this.in.readUTF()) != null) { // read new message
                System.out.println("[RKchat] " + message); // print the message to the console
            }
        } catch (Exception e) {
            System.err.println("[system] could not read message");
            e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}
