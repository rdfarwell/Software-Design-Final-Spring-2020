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
    private final String ID_1 = "1";
    private final String ID_2 = "2";
    private final JTextArea displayArea;
    private final JButton hit;
    private final hitListener hitListener = new hitListener();
    private final JButton stand;
    private final standListener standListener = new standListener();
    private final JPanel panel;
    private final JPanel subPanel;
    private Socket connection;
    private Scanner input;
    private Formatter output;
    private final String BlackJackHost;
    private String myID;
    private boolean myTurn;

    public Client(String host) {
        super("Blackjack");
        BlackJackHost = host;
        panel = new JPanel();
        subPanel = new JPanel();
        displayArea = new JTextArea(14, 30);
        displayArea.setEditable(false);
        //set background color of board to green
        Color color = new Color(31, 170, 42);
        displayArea.setBackground(color);
        //set text color to white
        displayArea.setForeground(Color.WHITE);
        hit = new JButton("Hit");
        hit.addActionListener(hitListener);
        stand = new JButton("Stand");
        stand.addActionListener(standListener);
        panel.setLayout(new BorderLayout());
        subPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        panel.add(displayArea, BorderLayout.NORTH);
        subPanel.add(hit);
        subPanel.add(stand);
        panel.add(subPanel, BorderLayout.SOUTH);
        add(panel);
        setSize(300, 300);
        setVisible(true);

        startClient();
    }

    public void startClient() {
        //connect to server and get streams
        try {
            //make connection to server - port range: 23503 - 23508
            connection = new Socket(InetAddress.getByName(BlackJackHost), 23503);

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
        //get player's ID (1 or 2)
        myID = input.nextLine();

        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        //display player's mark
                        displayArea.setText("You are player \"" + myID + "\"");
                    }
                }
        );

        //determine if client's turn
        myTurn = (myID.equals(ID_1));
        System.out.println((myID.equals(ID_1)));

        //call dealHand to deal 2 cards to each player + 1 card to dealer
        output.format("dealHand\n");
        output.flush();

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
        if (message.contains("output:"))
        {
            message = message.replace("output: ", "");
            displayMessage("\n" + message);
        }
        if (message.contains("end:")) {
            message = message.replace("end: ", "");
            displayMessage("\n" + message);
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

    private class hitListener implements ActionListener {
        /**
         * Outputs a "hit\n" formatted message to server to make a "hit" on player's thread.
         * @param event the event
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            if (myTurn = true) {
                output.format("hit\n");
                output.flush();
            }
        }
    }

    private class standListener implements ActionListener {
        /**
         * Outputs a "stand\n" formatted message to server to make a "stand" on player's thread.
         * @param event the event
         */
        @Override
        public void actionPerformed(ActionEvent event) {
            if (myTurn = true) {
                output.format("stand\n");
                output.flush();
                myTurn = false;
            }
        }
    }
}
