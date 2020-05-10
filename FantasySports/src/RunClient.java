import javax.swing.*;

/**
 * Runs the client.
 */
public class RunClient {
    /**
     * The entry point of application.
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Client client;
        // if no command line args
        if (args.length == 0)
            client = new Client("127.0.0.1"); // localhost
        else
            client = new Client(args[0]); // use args
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}