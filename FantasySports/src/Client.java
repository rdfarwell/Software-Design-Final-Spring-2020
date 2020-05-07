import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client extends JFrame implements Runnable {
    private final JTextArea displayArea;
    private final JPanel panel;
    private Socket connection;
    private Scanner input;
    private Formatter output;
    private final String Host;
    private String myID;

    public Client(String host) {
        super("Fantasy");
        Host = host;
        panel = new JPanel();
        displayArea = new JTextArea(14, 30);
        displayArea.setEditable(false);
        panel.setLayout(new BorderLayout());
        panel.add(displayArea, BorderLayout.NORTH);
        add(panel);
        setSize(300, 300);
        setVisible(true);

        startClient();
    }

    public void startClient() {
        //connect to server and get streams
        try {
            //make connection to server - port range: 23503 - 23508
            connection = new Socket(InetAddress.getByName(Host), 23504);

            //get streams for input and output
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        //create and start worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this); // execute client
    }

    public void run() {
        myID = input.nextLine();

        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        //display player's mark
                        displayArea.setText("You are player \"" + myID + "\"");
                    }
                }
        );

        //receive messages sent to client and output them
        while (true) {
            if (input.hasNextLine())
                processMessage(input.nextLine());
        }
    }

    private void processMessage(String message) {
        if (message.contains("hit:"))
        {
            message = message.replace("hit: ", "");
            displayMessage("\nYou got a " + message);
            displayMessage("\nWould you like to Hit or Stand?");
        }
    }

    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        displayArea.append(messageToDisplay);
                    }
                }
        );
    }
}
