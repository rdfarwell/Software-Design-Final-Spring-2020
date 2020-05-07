import javax.swing.*;

public class RunServer {
    public static void main(String[] args) {
        Server server = new Server();
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.execute();
    }
}