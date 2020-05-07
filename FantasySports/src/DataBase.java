import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class DataBase {
    private static File file = new File("FantasySports/database.csv");

    public static void main(String[] args) {
        addFields();
    }

    public static void addFields() {
        try {
            String[][] rows = new String[3][3];
            rows[0] = new String[] {"Dean", "DataBase", "Yes"};
            rows[1] = new String[] {"Alex", "Extra", "No"};
            rows[2] = new String[] {"Nolan", "BackBone", "Test"};

            FileWriter output = new FileWriter(file);
            output.append("Name");
            output.append(",");
            output.append("Role");
            output.append(",");
            output.append("Stat");
            output.append("\n");

            for (String[] row : rows) {
                output.append(String.join(",", row));
                output.append("\n");
            }

            output.flush();
            output.close();
        }
        catch (IOException e) {
            System.out.println("Error writing to file");
        }
    }
}