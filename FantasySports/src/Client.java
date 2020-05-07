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
    private final JScrollPane scroll;
    private final JTextArea displayArea;
    private final JPanel panel;
    private final JPanel subPanel;
    private final JButton chat;
    private Socket connection;
    private Scanner input;
    private Formatter output;
    private final String Host;
    private String myID;
    private final buttonListener hitListener = new buttonListener();

    public Client(String host) {
        super("Fantasy Overwatch");
        Host = host;
        panel = new JPanel();
        subPanel = new JPanel(new FlowLayout());
        chat = new JButton("Chat");
        displayArea = new JTextArea(13, 35);
        displayArea.setEditable(false);
        scroll = new JScrollPane(displayArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scroll, BorderLayout.CENTER);
        add(panel, BorderLayout.WEST);
        subPanel.add(chat, BorderLayout.CENTER);
        add(subPanel, BorderLayout.SOUTH);
        setSize(393, 290);
        setResizable(false);
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
                        displayArea.setText("You are player " + myID + "");
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
        if (message.contains("output:")) {
            message = message.replace("output: ", "");
            displayMessage(message);
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

    private class buttonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            if ( true) {
                output.format("hit\n");
                output.flush();
            }
        }
    }
}
