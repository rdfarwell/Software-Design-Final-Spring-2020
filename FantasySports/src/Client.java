import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Client.
 */
public class Client extends JFrame implements Runnable {
    /**
     * The scroll pane to show all messages to client.
     */
    private final JScrollPane scroll;
    /**
     * JTextArea nested in scroll pane.
     */
    private final JTextArea displayArea;
    /**
     * JTextField to take in user input.
     */
    private final JTextField inputArea;
    /**
     * JPanel to hold the scroll pane.
     */
    private final JPanel topPanel;
    /**
     * JPanel to hold the JTextField and JButton.
     */
    private final JPanel bottomPanel;
    /**
     * JButton to send a message typed in the JTextField.
     */
    private final JButton send;
    /**
     * Listener to send message when the button is clicked, or enter key is pressed.
     */
    private ActionListener buttonListener = new buttonListener();
    /**
     * Connection to server.
     */
    private Socket connection;
    /**
     * Gets input streams from server.
     */
    private BufferedReader input;
    /**
     * Sends outputs streams from client.
     */
    private PrintWriter output;
    /**
     * Name of host for server.
     */
    private final String Host;
    /**
     * ID number of the client.
     */
    private String myID;

    /**
     * Instantiates a new Client.
     * @param host the host
     */
    public Client(String host) {
        super("Fantasy Overwatch");
        Host = host;
        topPanel = new JPanel();
        bottomPanel = new JPanel(new FlowLayout());
        send = new JButton("Send");
        send.addActionListener(buttonListener);
        displayArea = new JTextArea(13, 41);
        displayArea.setEditable(false);
        inputArea = new JTextField("", 20);
        scroll = new JScrollPane(displayArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        topPanel.add(scroll, BorderLayout.WEST);
        add(topPanel, BorderLayout.WEST);
        bottomPanel.add(inputArea, BorderLayout.EAST);
        bottomPanel.add(send, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        //adds the ability to press the enter key to "click" the send button
        getRootPane().setDefaultButton(send);

        setSize(450, 295);
        setResizable(false);
        setVisible(true);
        startClient();
    }

    /**
     * Start client thread.
     */
    public void startClient() {
        //connect to server and get streams
        try {
            //make connection to server - port range: 23503 - 23508
            connection = new Socket(InetAddress.getByName(Host), 23504);

            //get streams for input and output
            input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            output = new PrintWriter(connection.getOutputStream(), true);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
        }

        //create and start worker thread for this client
        ExecutorService worker = Executors.newFixedThreadPool(1);
        worker.execute(this); // execute client
    }

    /**
     * Runs the client thread.
     */
    public void run() {
        //Get the ID number for the client
        try {
            myID = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Display to client what player they are
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
            try {
                processMessage(input.readLine());
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Deciphers the messages sent to the client.
     * @param message the message to be deciphered
     */
    private void processMessage(String message) {
        if (message.contains("message:")) {
            message = message.replace("message: ", "");
            displayMessage("\n" + message);
        }
        else if (message.contains("draft:")) {
            message = message.replace("draft: ", "");
            displayMessage("\n" + message);
        }
        else if (message.contains("trade:")) {
            message = message.replace("trade: ", "");
            displayMessage("\n" + message);
        }
    }

    /**
     * Displays deciphered messages to the scroll pane.
     * @param messageToDisplay the message
     */
    private void displayMessage(final String messageToDisplay) {
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        displayArea.append(messageToDisplay);
                    }
                }
        );
    }

    /**
     * Sends the client's message to the server on action (click, or enter key pressed).
     */
    private class buttonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            output.format(inputArea.getText() + "\n");
            output.flush();
            inputArea.setText("");
        }
    }
}
