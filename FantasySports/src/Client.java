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

public class Client extends JFrame implements Runnable {
    private final JScrollPane scroll;
    private final JTextArea displayArea;
    private final JTextField inputArea;
    private final JPanel leftPanel;
    private final JPanel rightPanel;
    private final JPanel subPanel;
    private final JButton send;
    private ActionListener buttonListener = new buttonListener();
    private Socket connection;
    private BufferedReader input;
    private PrintWriter output;
    private final String Host;
    private String myID;
    private final buttonListener hitListener = new buttonListener();

    public Client(String host) {
        super("Fantasy Overwatch");
        Host = host;
        leftPanel = new JPanel();
        rightPanel = new JPanel();
        subPanel = new JPanel(new FlowLayout());
        send = new JButton("Send");
        send.addActionListener(buttonListener);
        displayArea = new JTextArea(13, 35);
        displayArea.setEditable(false);
        inputArea = new JTextField("", 20);
//        inputArea.setEditable(false); //make initially false until server accepts all users
        scroll = new JScrollPane(displayArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        leftPanel.add(scroll, BorderLayout.WEST);
        rightPanel.add(inputArea, BorderLayout.EAST);
        add(leftPanel, BorderLayout.WEST);
        subPanel.add(rightPanel, BorderLayout.EAST);
        subPanel.add(send, BorderLayout.CENTER);
        add(subPanel, BorderLayout.SOUTH);
        setSize(393, 295);
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

    public void run() {
        try {
            myID = input.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void processMessage(String message) {
        if (message.contains("output:")) {
            message = message.replace("output: ", "");
            displayMessage(message);
        }
        else if (message.contains("message:")) {
            message = message.replace("message: ", "");
            displayMessage(message);
        }
        else if (message.contains("draft:")) {
            message = message.replace("draft: ", "");
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
            output.format(inputArea.getText() + "\n");
            output.flush();
        }
    }
}
