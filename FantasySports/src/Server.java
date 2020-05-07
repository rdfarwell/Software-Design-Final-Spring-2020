import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Server extends JFrame {
    private final JScrollPane scroll;
    private final JTextArea outputArea;
    private final Player[] players = new Player[4];
    private ServerSocket server;
    private final ExecutorService runGame;
    private final Lock gameLock;
    private Condition playersConnected;
    private boolean gameOver;

    public Server() {
        //set title of window
        super("Fantasy Server");
        //create ExecutorService with a thread for each player
        runGame = Executors.newFixedThreadPool(4);
        //create lock for game
        gameLock = new ReentrantLock();

        //set up ServerSocket - port range: 23503 - 23508
        try {
            server = new ServerSocket(23504, 4);
        }
        catch (IOException ioException) {
            ioException.printStackTrace();
            System.exit(1);
        }

        //create JTextArea for output
        outputArea = new JTextArea();
        outputArea.setText("Server awaiting connections\n");
        outputArea.setEditable(false);
        scroll = new JScrollPane(outputArea);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        add(scroll, BorderLayout.CENTER);
        setSize(500, 300);
        setVisible(true);
    }

    public void execute() {
        //wait for each client to connect
        for (int i = 0; i < players.length; i++) {
            //wait for connection, create Player, start runnable
            try {
                players[i] = new Player(server.accept(), i);
                //execute player runnable
                runGame.execute(players[i]);
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void displayMessage(final String messageToDisplay) {
        //display message from event-dispatch thread of execution
        SwingUtilities.invokeLater(
                new Runnable() {
                    //updates outputArea
                    public void run() {
                        //add message
                        outputArea.append(messageToDisplay);
                    }
                }
        );
    }

    private class Player implements Runnable {
        private final Socket connection;
        private Scanner input;
        private Formatter output;
        private final int playerNumber;
        private boolean suspended = true;

        public Player(Socket socket, int number) {
            playerNumber = number; //store this player's number
            connection = socket; //store socket for client

            //obtain streams from Socket
            try {
                input = new Scanner(connection.getInputStream());
                output = new Formatter(connection.getOutputStream());
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
        }

        public void run() {
            try {
                displayMessage("Player " + (playerNumber + 1) +  " connected\n");
                //send player's mark
                output.format("%s\n", (playerNumber + 1));
                output.flush();

                //wait for other players
                if (playerNumber == 0) {
                    gameLock.lock();
                    try {
                        while (suspended) {
                            playersConnected.await();
                        }
                    }
                    catch (InterruptedException exception) {
                        exception.printStackTrace();
                    }
                    //unlock game after all players have joined
                    finally {
                        gameLock.unlock();
                    }

                    //send message that other player connected
                    output.format("output: All Players connected\n");
                    output.flush();

                    //output the dealer's first card to player 1
                    output.format("output: Draft Starting\n");
                    output.flush();
                }
                else {
                    output.flush();
                }

                //temp string to get input from client
                String inputString;

                // while game not over
                while (!gameOver) {
                    inputString = input.nextLine();

                    if (inputString.equals("draft:")) {
                        output.format("output: You drafted: " + "\n");
                        output.flush();
                    }
                }
            }

            //close connection to client
            finally {
                try {
                    connection.close();
                }
                catch (IOException ioException) {
                    ioException.printStackTrace();
                    System.exit(1);
                }
            }
        }

        /**
         * Sets whether or not thread is suspended
         * @param status the status of suspension
         */
        public void setSuspended(boolean status) {
            //set value of suspended
            suspended = status;
        }
    }
}
