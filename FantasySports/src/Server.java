import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
    private String[] drafted = {"", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    private int draftCount = 0;
    private static ArrayList<Trade> trades = new ArrayList<>();
    private static final HashSet<PrintWriter> connectedPlayers = new HashSet<PrintWriter>();
    private static HashMap<Integer, Boolean> playerReady = new HashMap<Integer, Boolean>();

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
        } catch (IOException ioException) {
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
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
        }
    }

    private class Player implements Runnable {
        private final Socket connection;
        private BufferedReader input;
        private PrintWriter output;
        private final int playerNumber;
        private boolean suspended = true;
        private Team team = new Team();
        private int wins = 0;

        public Player(Socket socket, int number) {
            playerNumber = number + 1; //store this player's number
            connection = socket; //store socket for client

            //obtain streams from Socket
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioException) {
                ioException.printStackTrace();
                System.exit(1);
            }
            playerReady.put(playerNumber, false);
        }

        public Team getTeam() {
            return team;
        }

        public void addWin() {
            wins++;
        }

        //run the player thread
        public void run() {
            try {
                displayMessage("Player " + playerNumber + " connected\n");
                //send player's ID number
                output.format("%s\n", playerNumber);
                output.flush();
                //add the current player to the list of "outputable" clients
                connectedPlayers.add(output);

                //lock game on first join
                if (playerNumber == 1) {
                    gameLock.lock();
                }
                //display all players connected
                else if (playerNumber == 4) {
                    //Let players know all players are connected and give help message
                    for (PrintWriter writer : connectedPlayers) {
                        writer.println("message: All players connected, type @help for help\n");
                    }
                    displayMessage("All Players connected\n");
                }

                //temp string to get input from client
                String inputString = null;

                while (!gameOver) {
                    inputString = input.readLine();
                    if (inputString == null) {
                        output.format("\n");
                        output.flush();
                    }
                    //format message if player wants to draft
                    else if (inputString.contains("@draft")) {

                        String draftAttempt = inputString.replace("@draft", "").trim().toUpperCase();

                        if (inputString.contains("auto")) {
                            draftAttempt = Draft.auto(drafted).toUpperCase().trim();
                        }

                        System.out.println(Arrays.toString(drafted));

                        if (!team.fullTeam()) {
                            if (currentPlayer + 1 == playerNumber) {
                                if (Draft.validName(draftAttempt)) {
                                    if (Draft.draftable(drafted, draftAttempt)) {
                                        drafted[draftCount] = draftAttempt;
                                        draftCount++;

                                        team.addTeamMate(draftAttempt); //add the drafted player to the players team

                                        output.format("draft: You drafted: " + draftAttempt + "\n");
                                        output.format("draft: " + team.toString() + "\n");
                                        output.flush();

                                        currentPlayer = Draft.updateCurrentPlayer(currentPlayer);

                                        for (PrintWriter writer : connectedPlayers) {
                                            writer.println("message: player " + playerNumber + " drafted: " + draftAttempt);

                                            if (playerNumber == 1 && team.fullTeam()) {
                                                writer.println("message: Draft is done!");
//                                                for (Player p : players) {
//                                                    writer.println("Player " + p.playerNumber + " " + p.team.toString());
//                                                }
                                            }
                                        }
                                    } else {
                                        output.format("draft: Character has already been drafted \n");
                                        output.flush();
                                    }
                                } else {
                                    output.format("draft: Invalid entry \n");
                                    output.flush();
                                }
                            } else {
                                output.format("draft: Not your turn \n");
                                output.flush();
                            }
                        } else {
                            output.format("draft: Your team is full \n");
                            output.flush();
                        }
                        displayMessage("\nplayer " + playerNumber + ": " + inputString);
                    }
                    //format message if player wants to trade
                    else if (inputString.contains("@trade")) {
                        try {
                            String tradeAttempt = inputString.replace("@trade", "").trim().toUpperCase();
                            String[] tradeStuff = tradeAttempt.split(",");
                            String playerTo = tradeStuff[0].trim(), toTrade = tradeStuff[1].trim(), toReceive = tradeStuff[2].trim();

                            if (players[Integer.parseInt(playerTo) - 1].getTeam().hasCharacter(toReceive)) {
                                trades.add(new Trade(playerNumber, Integer.parseInt(playerTo), toTrade, toReceive));
                                for (PrintWriter writer : connectedPlayers) {
                                    if (writer == players[Integer.parseInt(playerTo) - 1].output) {
                                        writer.println("message: player " + playerNumber + " has requested a trade, " + toReceive + " for " + toTrade + " \n ");
                                        displayMessage("player " + playerNumber + " has requested a trade, " + toReceive + " for " + toTrade + " \n ");
                                    }
                                }
                            } else {
                                output.format("trade: player does not have that character");
                                output.flush();
                            }
                        } catch (ArrayIndexOutOfBoundsException bound) {
                            try {
                                Trade tempTrade = null;
                                for (Trade trade : trades) {
                                    if (trade.getReceiver() == playerNumber) {
                                        System.out.println("trade init");
                                        tempTrade = trade;
                                    }
                                }
                                System.out.println("got trade");

                                if (tempTrade != null) {
                                    if (inputString.toLowerCase().contains("accept")) {
                                        System.out.println("accepted");
                                        players[playerNumber - 1].getTeam().trade(tempTrade.getWant(), tempTrade.getOffer());
                                        System.out.println("trade1");
                                        players[tempTrade.getSender() - 1].getTeam().trade(tempTrade.getOffer(), tempTrade.getWant());
                                        System.out.println("trade2");
                                        for (PrintWriter writer : connectedPlayers) {
                                            if (writer == players[tempTrade.getSender() - 1].output) {
                                                writer.println("message: player " + playerNumber + " has accepted your trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                                displayMessage("player " + playerNumber + " has accepted trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                            }
                                        }
                                    } else if (inputString.toLowerCase().contains("deny")) {
                                        for (PrintWriter writer : connectedPlayers) {
                                            if (writer == players[tempTrade.getSender() - 1].output) {
                                                writer.println("message: player " + playerNumber + " has denied your trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                                displayMessage("player " + playerNumber + " has denied trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                            }
                                        }
                                    }
                                    trades.remove(tempTrade);

                                    System.out.println("finished trade");
                                }
                            } catch (NullPointerException notinit) {
                                output.format("trade: You have no open trades\n");
                                output.flush();
                            }
                        }

                        displayMessage("\nplayer " + playerNumber + ": " + inputString);
                    } else if (inputString.contains("@ready")) {
                        for (PrintWriter writer : connectedPlayers) {
                            writer.println("message: Player " + playerNumber + " has readied up");
                        }
                        playerReady.replace(playerNumber, true);
                        boolean readyToStart = false;
                        for (int player : playerReady.keySet()) {
                            if (!playerReady.get(player)) {
                                readyToStart = false;
                            } else {
                                readyToStart = true;
                            }
                        }

                        if (readyToStart && Score.getCurrentWeek() <= 6) {
                            for (PrintWriter writer : connectedPlayers) {
                                writer.println("message: Week " + Score.getCurrentWeek() + " has begun");
                            }
                            //Display message to server log
                            displayMessage("Week " + Score.getCurrentWeek() + " has begun");
                            for (Player player : players) {
                                player.getTeam().resetWeeklyScore();
                                player.getTeam().addScore();
                            }
                            if (Score.getCurrentWeek() == 1 || Score.getCurrentWeek() == 4) {
                                for (PrintWriter writer : connectedPlayers) {
                                    if (writer == players[0].output) {
                                        writer.println("message: You are facing player 2, with team, " + players[1].getTeam());
                                        writer.println("message: Your team scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[0].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[1].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[1].getTeam().getCharScore());
                                        if (players[0].getTeam().getWeeklyScore() < players[1].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[0].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[1].output) {
                                        writer.println("message: You are facing player 1, with team, " + players[0].getTeam());
                                        writer.println("message: Your team scored " + players[1].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[1].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[0].getTeam().getCharScore());
                                        if (players[1].getTeam().getWeeklyScore() < players[0].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[1].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[2].output) {
                                        writer.println("message: You are facing player 4, with team, " + players[3].getTeam());
                                        writer.println("message: Your team scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[2].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[3].getTeam().getCharScore());
                                        if (players[2].getTeam().getWeeklyScore() < players[3].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[2].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[3].output) {
                                        writer.println("message: You are facing player 3, with team, " + players[2].getTeam());
                                        writer.println("message: Your team scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[3].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[2].getTeam().getCharScore());
                                        if (players[3].getTeam().getWeeklyScore() < players[2].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[3].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    }
                                }
                                Score.currentWeekPlus();
                            } else if (Score.getCurrentWeek() == 2 || Score.getCurrentWeek() == 5) {
                                for (PrintWriter writer : connectedPlayers) {
                                    if (writer == players[0].output) {
                                        writer.println("message: You are facing player 3, with team, " + players[2].getTeam());
                                        writer.println("message: Your team scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[0].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[2].getTeam().getCharScore());
                                        if (players[0].getTeam().getWeeklyScore() < players[2].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[0].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[1].output) {
                                        writer.println("message: You are facing player 4, with team, " + players[3].getTeam());
                                        writer.println("message: Your team scored " + players[1].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[1].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[3].getTeam().getCharScore());
                                        if (players[1].getTeam().getWeeklyScore() < players[3].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[1].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[2].output) {
                                        writer.println("message: You are facing player 1, with team, " + players[0].getTeam());
                                        writer.println("message: Your team scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[2].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[0].getTeam().getCharScore());
                                        if (players[2].getTeam().getWeeklyScore() < players[0].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[2].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[3].output) {
                                        writer.println("message: You are facing player 2, with team, " + players[1].getTeam());
                                        writer.println("message: Your team scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[3].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[1].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[1].getTeam().getCharScore());
                                        if (players[3].getTeam().getWeeklyScore() < players[1].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[3].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    }
                                }
                                Score.currentWeekPlus();
                            } else if (Score.getCurrentWeek() == 3 || Score.getCurrentWeek() == 6) {
                                for (PrintWriter writer : connectedPlayers) {
                                    if (writer == players[0].output) {
                                        writer.println("message: You are facing player 4, with team, " + players[3].getTeam());
                                        writer.println("message: Your team scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[0].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[3].getTeam().getCharScore());
                                        if (players[0].getTeam().getWeeklyScore() < players[3].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[0].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[1].output) {
                                        writer.println("message: You are facing player 3, with team, " + players[2].getTeam());
                                        writer.println("message: Your team scored " + players[1].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[1].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[2].getTeam().getCharScore());
                                        if (players[1].getTeam().getWeeklyScore() < players[2].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[1].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[2].output) {
                                        writer.println("message: You are facing player 2, with team, " + players[1].getTeam());
                                        writer.println("message: Your team scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[2].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[1].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[1].getTeam().getCharScore());
                                        if (players[2].getTeam().getWeeklyScore() < players[1].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[2].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    } else if (writer == players[3].output) {
                                        writer.println("message: You are facing player 1, with team, " + players[0].getTeam());
                                        writer.println("message: Your team scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually your characters scored " + players[3].getTeam().getCharScore());
                                        writer.println("message: Your opponent scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                        writer.println("message: Individually their characters scored " + players[0].getTeam().getCharScore());
                                        if (players[3].getTeam().getWeeklyScore() < players[0].getTeam().getWeeklyScore()) {
                                            writer.println("message: You won this week");
                                            players[3].addWin();
                                        } else {
                                            writer.println("message: You lost this week");
                                        }
                                    }
                                }
                            }
                            Score.currentWeekPlus();
                        } else if (Score.getCurrentWeek() > 6) {
                            Player[] winners = players;
                            for (int i = 0; i < 4; i++) {
                                for (int j = 0; j < 4; j++) {
                                    if (players[i].getTeam().getTotalScore() > winners[j].getTeam().getTotalScore()) {
                                        for (int z = 3; z > j; z--) {
                                            winners[z] = winners[z - 1];
                                        }
                                        winners[j] = players[i];
                                    }
                                }
                            }
                            for (PrintWriter writer : connectedPlayers) {
                                writer.println("message: 1st " + winners[0] + ", 2nd " + winners[1] + ", 3rd " + winners[2] + ", 4th " + winners[3]);
                            }
                        }
                    }

                    // chat feature for sending PM's
                    else if (inputString.contains("@player")) {
                        for (PrintWriter writer : connectedPlayers) {
                            if (inputString.contains("1") && writer == players[0].output) {
                                writer.println("message: From player " + playerNumber + ": " + inputString.replace("@player", "").replace("1", "").trim());
                            } else if (inputString.contains("2") && writer == players[1].output) {
                                writer.println("message: From player " + playerNumber + ": " + inputString.replace("@player", "").replace("2", "").trim());
                            } else if (inputString.contains("3") && writer == players[2].output) {
                                writer.println("message: From player " + playerNumber + ": " + inputString.replace("@player", "").replace("3", "").trim());
                            } else if (inputString.contains("4") && writer == players[3].output) {
                                writer.println("message: From player " + playerNumber + ": " + inputString.replace("@player", "").replace("4", "").trim());
                            }
                        }
                    } else if (inputString.contains("@help")) {
                        for (PrintWriter writer : connectedPlayers) {
                            if (writer == players[playerNumber - 1].output) {
                                writer.println("message: @draft character - drafts the character you entered to your team");
                                writer.println("message: @draft auto - automatically picks a character for you");
                                writer.println("message: @trade playerNumber, character you have to trade, character you want - sends playerNumber a message stating you want to trade said characters");
                                writer.println("message: @trade accept - accepts the trade you were sent");
                                writer.println("message: @trade deny - denies the trade you were sent");
                                writer.println("message: @player playerNumber - sends a message to player associated with playerNumber");
                                writer.println("message: @ready - tells the server that you are ready for the current weeks competition to run");
                            }
                        }
                    }
                    //a standard message from a specific player
                    else {
                        for (PrintWriter writer : connectedPlayers) {
                            writer.println("message: player " + playerNumber + ": " + inputString);
                        }
                        //display message to server for log
                        displayMessage("\nplayer " + playerNumber + ": " + inputString);
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
                } catch (IOException ioException) {
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
