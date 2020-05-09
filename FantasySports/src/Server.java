import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
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
    private final Condition playersConnected;
    private boolean gameOver = false;
    private int currentPlayer;
    private static final HashSet<PrintWriter> connectedPlayers = new HashSet<PrintWriter>();

    public Server() {
        //set title of window
        super("Fantasy Server");
        //create ExecutorService with a thread for each player
        runGame = Executors.newFixedThreadPool(4);
        //create lock for game
        gameLock = new ReentrantLock();
        //condition for all players being connected
        playersConnected = gameLock.newCondition();
        //set the starting player
        currentPlayer = 0;

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

//        gameLock.lock();
//        try {
//            //resume player 1
//            players[0].setSuspended(false);
//            //wake up player 1's thread
//            player1Turn.signal();
//        }
//        finally {
//            //unlock game after signalling player 1
//            gameLock.unlock();
//        }
    }

    private class Player implements Runnable {
        private final Socket connection;
        private BufferedReader input;
        private PrintWriter output;
        private final int playerNumber;
        private boolean suspended = true;

        public Player(Socket socket, int number) {
            playerNumber = number + 1; //store this player's number
            connection = socket; //store socket for client

            //obtain streams from Socket
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            }
            catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
        }

        //run the player thread
        public void run() {
            try {
                displayMessage("Player " + playerNumber +  " connected\n");
                //send player's ID number
                output.format("%s\n", playerNumber);
                output.flush();

                //lock game on first join
                if (playerNumber == 1) {
                    gameLock.lock();
                }
                //display all players connected
                else if (playerNumber == 4) {
                    displayMessage("All Players connected\n");
                }

                //temp string to get input from client
                String inputString = null;
                //add the current player to the list of "outputable" clients
                connectedPlayers.add(output);

                while (!gameOver) {
                    inputString = input.readLine();
                    if (inputString == null) {
                        output.format("\n");
                        output.flush();
                    }
                    //format message if player wants to draft
                    if (inputString.contains("@draft")) {
                        output.format("draft: You drafted:  \n");
                        output.flush();
                        //display message to server for log
                        displayMessage(inputString);
                    }
                    //format message if player wants to trade
                    if (inputString.contains("@trade")) {
                        output.format("trade: \n");
                        output.flush();
                        //display message to server for log
                        displayMessage(inputString);
                    }
                    //a standard message from a specific player
                    else {
                        for (PrintWriter writer : connectedPlayers) {
                            writer.println("message: player " + playerNumber + ": " + inputString);
                        }
                        //display message to server for log
                        displayMessage("player " + playerNumber + ": " + inputString);
                    }
                }
            }

            //catch exceptions for input.readLine()
            catch (IOException e) {
                e.printStackTrace();
            }

            //close connection to client
            finally {
                try {
                    input.close();
                    output.close();
                    connection.close();
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
    }
}
