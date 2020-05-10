import javax.swing.*;

/**
 * Runs the server.
 */
public class RunServer {
    /**
     * The entry point of application.
     * @param args the input arguments
     */
    public static void main(String[] args) {
        Server server = new Server();
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.execute();
    }
}