import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
        /**
         * init of the team class for the player so each player has its own team
         */
        private Team team = new Team();
        /**
         * the number of wins the player has
         */
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

        /**
         * method getTeam returns the team related to the player
         * @return Team the players Team
         */
        public Team getTeam() {
            return team;
        }

        /**
         * method addWin to add a win to a player
         */
        public void addWin() {
            wins++;
        }

        /**
         * method get wins returns the players wins
         * @return int the players wins
         */
        public int getWins() {
            return wins;
        }

        //run the player thread

        /**
         * the method that runs the server and takes in the clients input and reads it to allow the correct response, i.e. seeing @draft [character] will read it in and draft that character and add it to the players neam
         */
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
                        writer.println("message: Draft is beginning, Player 1's turn\n");
                    }
                    displayMessage("All Players connected\n");
                }

                //temp string to get input from client
                String inputString;

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
                        //check to see if player is eligible to draft a character
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
                                            writer.println("message: Player " + playerNumber + " drafted: " + draftAttempt);

                                            //Once the draft is complete send message to all players, and inform them to ready up
                                            if (playerNumber == 1 && team.fullTeam()) {
                                                writer.println("message: Draft is done! Type @ready to start");
                                                //display message to server log that draft is done
                                                displayMessage("\nDraft is done, waiting for players to ready up");
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
                        displayMessage("\nPlayer " + playerNumber + ": " + inputString);
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
                                        writer.println("message: Player " + playerNumber + " has requested a trade, " + toReceive + " for " + toTrade + " \n ");
                                        displayMessage("\nPlayer " + playerNumber + " has requested a trade, " + toReceive + " for " + toTrade + " \n ");
                                    }
                                }
                            } else {
                                output.format("trade: Player does not have that character\n");
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
                                                writer.println("message: Player " + playerNumber + " has accepted your trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                                displayMessage("\nPlayer " + playerNumber + " has accepted trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                            }
                                        }
                                    } else if (inputString.toLowerCase().contains("deny")) {
                                        for (PrintWriter writer : connectedPlayers) {
                                            if (writer == players[tempTrade.getSender() - 1].output) {
                                                writer.println("message: Player " + playerNumber + " has denied your trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                                displayMessage("\nPlayer " + playerNumber + " has denied trade of, " + tempTrade.getOffer() + " for " + tempTrade.getWant() + " \n ");
                                            }
                                        }
                                    }
                                    trades.remove(tempTrade);

                                    System.out.println("finished trade");
                                }
                            } catch (NullPointerException notInitialized) {
                                output.format("trade: You have no open trades\n");
                                output.flush();
                            }
                        } catch (NumberFormatException | NullPointerException nonValidEntry) {
                            output.format("message: Not a valid entry\n");
                            output.flush();
                        }

                        displayMessage("\nplayer " + playerNumber + ": " + inputString);
                    } else if (inputString.contains("@ready")) {
                        if (players[0].getTeam().fullTeam() && players[1].getTeam().fullTeam() && players[2].getTeam().fullTeam() && players[3].getTeam().fullTeam()) {
                            for (PrintWriter writer : connectedPlayers) {
                                writer.println("message: Player " + playerNumber + " has readied up");
                            }
                            playerReady.replace(playerNumber, true);
                            boolean readyToStart = false;
                            if (playerReady.get(1) && playerReady.get(2) && playerReady.get(3) && playerReady.get(4)) {
                                readyToStart = true;
                            }
                            if (readyToStart && Score.getCurrentWeek() <= 6) {
                                for (PrintWriter writer : connectedPlayers) {
                                    writer.println("message: Week " + Score.getCurrentWeek() + " has begun");
                                }
                                //Display message to server log
                                displayMessage("\nWeek " + Score.getCurrentWeek() + " has begun");
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
                                            if (players[0].getTeam().getWeeklyScore() > players[1].getTeam().getWeeklyScore()) {
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
                                            if (players[1].getTeam().getWeeklyScore() > players[0].getTeam().getWeeklyScore()) {
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
                                            if (players[2].getTeam().getWeeklyScore() > players[3].getTeam().getWeeklyScore()) {
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
                                            if (players[3].getTeam().getWeeklyScore() > players[2].getTeam().getWeeklyScore()) {
                                                writer.println("message: You won this week");
                                                players[3].addWin();
                                            } else {
                                                writer.println("message: You lost this week");
                                            }
                                        }
                                    }
                                    Score.currentWeekPlus();
                                    playerReady.replace(1, false);
                                    playerReady.replace(2, false);
                                    playerReady.replace(3, false);
                                    playerReady.replace(4, false);
                                } else if (Score.getCurrentWeek() == 2 || Score.getCurrentWeek() == 5) {
                                    for (PrintWriter writer : connectedPlayers) {
                                        if (writer == players[0].output) {
                                            writer.println("message: You are facing player 3, with team, " + players[2].getTeam());
                                            writer.println("message: Your team scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                            writer.println("message: Individually your characters scored " + players[0].getTeam().getCharScore());
                                            writer.println("message: Your opponent scored " + players[2].getTeam().getWeeklyScore() + " this week");
                                            writer.println("message: Individually their characters scored " + players[2].getTeam().getCharScore());
                                            if (players[0].getTeam().getWeeklyScore() > players[2].getTeam().getWeeklyScore()) {
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
                                            if (players[1].getTeam().getWeeklyScore() > players[3].getTeam().getWeeklyScore()) {
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
                                            if (players[2].getTeam().getWeeklyScore() > players[0].getTeam().getWeeklyScore()) {
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
                                            if (players[3].getTeam().getWeeklyScore() > players[1].getTeam().getWeeklyScore()) {
                                                writer.println("message: You won this week");
                                                players[3].addWin();
                                            } else {
                                                writer.println("message: You lost this week");
                                            }
                                        }
                                    }
                                    Score.currentWeekPlus();
                                    playerReady.replace(1, false);
                                    playerReady.replace(2, false);
                                    playerReady.replace(3, false);
                                    playerReady.replace(4, false);
                                } else if (Score.getCurrentWeek() == 3 || Score.getCurrentWeek() == 6) {
                                    for (PrintWriter writer : connectedPlayers) {
                                        if (writer == players[0].output) {
                                            writer.println("message: You are facing player 4, with team, " + players[3].getTeam());
                                            writer.println("message: Your team scored " + players[0].getTeam().getWeeklyScore() + " this week");
                                            writer.println("message: Individually your characters scored " + players[0].getTeam().getCharScore());
                                            writer.println("message: Your opponent scored " + players[3].getTeam().getWeeklyScore() + " this week");
                                            writer.println("message: Individually their characters scored " + players[3].getTeam().getCharScore());
                                            if (players[0].getTeam().getWeeklyScore() > players[3].getTeam().getWeeklyScore()) {
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
                                            if (players[1].getTeam().getWeeklyScore() > players[2].getTeam().getWeeklyScore()) {
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
                                            if (players[2].getTeam().getWeeklyScore() > players[1].getTeam().getWeeklyScore()) {
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
                                            if (players[3].getTeam().getWeeklyScore() > players[0].getTeam().getWeeklyScore()) {
                                                writer.println("message: You won this week");
                                                players[3].addWin();
                                            } else {
                                                writer.println("message: You lost this week");
                                            }
                                        }
                                    }
                                    Score.currentWeekPlus();
                                    playerReady.replace(1, false);
                                    playerReady.replace(2, false);
                                    playerReady.replace(3, false);
                                    playerReady.replace(4, false);
                                }
                            } else if (Score.getCurrentWeek() > 6) {
                                int[] needToFlip = new int[]{0, 0, 0, 0};
                                int[] wins = new int[]{0, 0, 0, 0};
                                int[] podium = new int[]{0, 0, 0, 0};

                                for (int p = 0; p < 4; p++) {
                                    needToFlip[p] = players[p].getWins();
                                }

                                Arrays.sort(needToFlip);

                                // flipped since we want descending order, but sort gives ascending
                                for (int i = 0; i < 4; i++) {
                                    wins[3 - i] = needToFlip[i];
                                }

                                boolean p1 = false, p2 = false, p3 = false, p4 = false;
                                for (int t = 0; t < 4; t++) {
                                    if (wins[t] == players[0].getWins() && !p1) {
                                        podium[t] = players[0].playerNumber;
                                        p1 = true;
                                    } else if (wins[t] == players[1].getWins() && !p2) {
                                        podium[t] = players[1].playerNumber;
                                        p2 = true;
                                    } else if (wins[t] == players[2].getWins() && !p3) {
                                        podium[t] = players[2].playerNumber;
                                        p3 = true;
                                    } else if (wins[t] == players[3].getWins() && !p4) {
                                        podium[t] = players[3].playerNumber;
                                        p4 = true;
                                    }
                                }

                                // Tiebreaker
                                for (int j = 0; j < 4; j++) {
                                    for (int k = 0; k < 4; k++) {
                                        if (j != k) {
                                            if (players[j].getWins() == players[k].getWins()) {
                                                int jp = 0, kp = 0;
                                                for (int x = 0; x < 4; x++) {
                                                    if (podium[x] == players[j].playerNumber) {
                                                        jp = x;
                                                    }
                                                    if (podium[x] == players[k].playerNumber) {
                                                        kp = x;
                                                    }
                                                }
                                                if (players[j].getTeam().getTotalScore() > players[k].getTeam().getTotalScore() && kp < jp) {
                                                    podium[kp] = players[j].playerNumber;
                                                    podium[jp] = players[k].playerNumber;
                                                } else if (players[k].getTeam().getTotalScore() > players[j].getTeam().getTotalScore() && jp < kp) {
                                                    podium[kp] = players[j].playerNumber;
                                                    podium[jp] = players[k].playerNumber;
                                                }
                                            }
                                        }
                                    }
                                }

                                for (PrintWriter writer : connectedPlayers) {
                                    writer.println("message: 1st Place: Player " + podium[0] + ", 2nd Place: Player " + podium[1] + ", 3rd Place: Player " + podium[2] + ", 4th Place: Player " + podium[3]);
                                }
                            }
                        } else {
                            output.format("message: Not all teams are full\n");
                            output.flush();
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

                    } else if (inputString.contains("@stats")) {
                        String characterName = inputString.replace("@stats", "").trim().toUpperCase();
                        String statsOut;
                        if (Draft.validName(characterName)) {
                            Character statsChar = new Character(characterName);
                            statsOut = "message: Stats for " + characterName + ": Offense: " + statsChar.getOffense() + ", Defense: " + statsChar.getSupport() + ", Support: " + statsChar.getSupport();
                        } else {
                            statsOut = "message: Invalid character name";
                        }
                        for (PrintWriter writer : connectedPlayers) {
                            if (writer == players[playerNumber - 1].output) {
                                writer.println(statsOut);
                            }
                        }
                    } else if (inputString.contains("@replace")) {
                        if (players[0].getTeam().fullTeam() && players[1].getTeam().fullTeam() && players[2].getTeam().fullTeam() && players[3].getTeam().fullTeam()) {
                            String replaceAttempt = inputString.replace("@replace", "").trim().toUpperCase();
                            String[] tradeStuff = replaceAttempt.split(",");
                            String toGive = tradeStuff[0].trim(), toReceive = tradeStuff[1].trim();
                            if (players[playerNumber - 1].getTeam().hasCharacter(toGive)) {
                                if (Draft.validName(toReceive) && Draft.draftable(drafted, toReceive)) {
                                    players[playerNumber - 1].getTeam().trade(toGive, toReceive);
                                    for (int i = 0; i < drafted.length; i++) {
                                        if (drafted[i].equals(toGive)) {
                                            drafted[i] = toReceive;
                                        }
                                    }
                                    for (PrintWriter writer : connectedPlayers) {
                                        if (writer == players[playerNumber - 1].output) {
                                            writer.println("message: You have successfully replaced " + toGive + " with " + toReceive + ".");
                                            writer.println("message: Your new team " + players[playerNumber - 1].getTeam().toString());
                                        }
                                    }
                                } else {
                                    for (PrintWriter writer : connectedPlayers) {
                                        if (writer == players[playerNumber - 1].output) {
                                            writer.println("message: You cannot draft that player.");
                                        }
                                    }
                                }
                            } else {
                                for (PrintWriter writer : connectedPlayers) {
                                    if (writer == players[playerNumber - 1].output) {
                                        writer.println("message: You do not have that character on your team.");
                                    }
                                }
                            }
                        } else {
                            output.format("message: Draft is not finished yet.\n");
                            output.flush();
                        }
                    } else if (inputString.contains("@characters")) {
                        output.format("message: " + Arrays.toString(DataBase.getData("Name")) + "\n");
                        output.flush();
                    } else if (inputString.contains("@open")) {
                        StringBuilder charOut = new StringBuilder();
                        for (String character : DataBase.getData("Name")) {
                            boolean charHasBeenDrafted = false;
                            for (String draftedChar : drafted) {
                                if (draftedChar.toUpperCase().equals(character.toUpperCase())) {
                                    charHasBeenDrafted = true;
                                    break;
                                }
                            }
                            if (!charHasBeenDrafted) {
                                charOut.append(character).append(", ");
                            }
                        }
                        output.format("message: " + charOut.toString() + "\n");
                        output.flush();
                    } else if (inputString.contains("@team")) {
                        String playerLookup = inputString.replace("@team", "").trim();
                        int playerNumberToSearch;
                        if (playerLookup.equals("")) {
                            playerNumberToSearch = playerNumber;
                            output.format("message: Player " + playerNumberToSearch + "`s Team: " + players[playerNumberToSearch-1].getTeam().toString() + "\n");
                            output.flush();
                        }
                        else {
                            try {
                                playerNumberToSearch = Integer.parseInt(playerLookup);
                                output.format("message: Player " + playerNumberToSearch + "`s Team: " + players[playerNumberToSearch-1].getTeam().toString() + "\n");
                                output.flush();
                            } catch (NumberFormatException | NullPointerException notInt) {
                                output.format("message: Not a valid entry\n");
                                output.flush();
                            }
                        }
                    } else if (inputString.contains("@score")) {
                        for (PrintWriter writer : connectedPlayers) {
                            if (writer == players[playerNumber - 1].output) {
                                writer.println("message: Player 1 Scores: Wins: " + players[0].getWins() + ", Total Score: " + players[0].getTeam().getTotalScore());
                                writer.println("message: Player 2 Scores: Wins: " + players[1].getWins() + ", Total Score: " + players[1].getTeam().getTotalScore());
                                writer.println("message: Player 3 Scores: Wins: " + players[2].getWins() + ", Total Score: " + players[2].getTeam().getTotalScore());
                                writer.println("message: Player 4 Scores: Wins: " + players[3].getWins() + ", Total Score: " + players[3].getTeam().getTotalScore());
                            }
                        }
                    } else if (inputString.contains("@help")) {
                        for (PrintWriter writer : connectedPlayers) {
                            if (writer == players[playerNumber - 1].output) {
                                writer.println("message: Typing in the entry bar (not using a code below), will send a message to all players");
                                writer.println("message: @team [playerNumber] - Lists the team associated with playerNumber (leaving playerNumber blank returns your team)");
                                writer.println("message: @score - Lists each player, their win total, and overall score");
                                writer.println("message: @character - Lists all characters in the game");
                                writer.println("message: @open - Lists all characters that are open to be drafted");
                                writer.println("message: @stats [character] - Gives the stats of the corresponding character");
                                writer.println("message: @draft [character] - drafts the character you entered to your team");
                                writer.println("message: @draft auto - automatically picks a character for you");
                                writer.println("message: @replace [character to replace, replacement character] - replaces a character on your team with another character");
                                writer.println("message: @trade [playerNumber, your character offer, character you want] - sends playerNumber a message stating you want to trade said characters");
                                writer.println("message: @trade accept - accepts the trade you were sent");
                                writer.println("message: @trade deny - denies the trade you were sent");
                                writer.println("message: @player [playerNumber] - sends a message to player associated with playerNumber");
                                writer.println("message: @ready - tells the server that you are ready for the current weeks competition to run");
                            }
                        }
                    }
                    //a standard message from a specific player
                    else {
                        for (PrintWriter writer : connectedPlayers) {
                            writer.println("message: Player " + playerNumber + ": " + inputString);
                        }
                        //display message to server for log
                        displayMessage("\nPlayer " + playerNumber + ": " + inputString);
                    }
                }
            }

            //catch exceptions for input.readLine()
            catch (
                    IOException e) {
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