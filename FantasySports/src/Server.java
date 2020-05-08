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
    private final Condition playersConnected;
    private final Condition player1Turn;
    private final Condition player2Turn;
    private final Condition player3Turn;
    private final Condition player4Turn;
    private final boolean draftRound;
    private boolean gameTime;
    private int currentPlayer;

    public Server() {
        //set title of window
        super("Fantasy Server");
        //create ExecutorService with a thread for each player
        runGame = Executors.newFixedThreadPool(4);
        //create lock for game
        gameLock = new ReentrantLock();
        //condition for all players being connected
        playersConnected = gameLock.newCondition();
        //condition variable for each player's turn
        player1Turn = gameLock.newCondition();
        player2Turn = gameLock.newCondition();
        player3Turn = gameLock.newCondition();
        player4Turn = gameLock.newCondition();
        //set the starting player
        currentPlayer = 0;
        //set the game into the draft round
        draftRound = true;

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

        gameLock.lock();
        try {
            //resume player 1
            players[0].setSuspended(false);
            //wake up player 1's thread
            player1Turn.signal();
        }
        finally {
            //unlock game after signalling player 1
            gameLock.unlock();
        }
    }

    private class Player implements Runnable {
        private final Socket connection;
        private Scanner input;
        private Formatter output;
        private final int playerNumber;
        private boolean suspended = true;

        public Player(Socket socket, int number) {
            playerNumber = number + 1; //store this player's number
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
                displayMessage("Player " + playerNumber +  " connected\n");
                //send player's ID number
                output.format("%s\n", playerNumber);
                output.flush();

                //wait for other players
                if (playerNumber == 1) {
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
                }
                else if (playerNumber == 4) {
                    displayMessage("All Players connected\n");
                }

                //temp string to get input from client
                String inputString;

                //TODO drafting round
                while (draftRound) {
                    inputString = input.nextLine();

                    if (inputString.contains("draft:")) {
                        output.format("draft: You drafted:  \n");
                        output.flush();
                    }
                }
                //TODO: After draft, start normal game time
                while (gameTime) {

                }
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

        /**
         * Sets whether or not thread is suspended
         * @param status the status of suspension
         */
        public void setSuspended(boolean status) {
            //set value of suspended
            suspended = status;
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

        public boolean validateDraftPick(String name, int player) {
            while (player != currentPlayer) {
                //lock game to wait for other player to go
                gameLock.lock();

                //wait for each player's turn
                try {
                    if (player == 1) {
                        player2Turn.await();
                        player3Turn.await();
                        player4Turn.await();
                    }
                    else if (player == 2) {
                        player1Turn.await();
                        player3Turn.await();
                        player4Turn.await();
                    }
                    else if (player == 3) {
                        player1Turn.await();
                        player2Turn.await();
                        player4Turn.await();
                    }
                    else if (player == 4) {
                        player1Turn.await();
                        player2Turn.await();
                        player3Turn.await();
                    }
                }
                catch (InterruptedException exception) {
                    exception.printStackTrace();
                }
                finally {
                    //unlock game after waiting
                    gameLock.unlock();
                }
            }

            //TODO add boolean method to check if player was drafted using the passed name
            if (!draftedPlayer(name)) {
                //change player
                currentPlayer = (currentPlayer + 1) % 4;

                //lock game to signal other player to go
                gameLock.lock();

                try {
                    //signal the next player
                    if (player == 1) {
                        player2Turn.signal();
                    }
                    else if (player == 2) {
                        player3Turn.signal();
                    }
                    else if (player == 3) {
                        player4Turn.signal();
                    }
                    else if (player == 4) {
                        player1Turn.signal();
                    }
                }
                finally {
                    //unlock game after signaling
                    gameLock.unlock();
                }
                //let player know that the player they selected was drafted successfully
                return (true);
            }
            else {
                return (false);
            }
        }

        //TODO: determine if a character has been drafted or not
        public boolean draftedPlayer(String name) {
            return true;
        }
    }
}
