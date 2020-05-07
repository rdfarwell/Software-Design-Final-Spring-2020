import javax.print.DocFlavor;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class DataBase {
    private static File file = new File("FantasySports/database.csv");
    private static String[][] data;
    private static final int NUMROWS = 3;
    private static final int NUMDATA = 3;

    public static void main(String[] args) {
        addFields();
        readFile();
        getData("Name");
    }

    public static void addFields() {
        try {
            String[][] rows = new String[NUMROWS][NUMDATA];
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

    public static void readFile() {
        String[][] output = new String[NUMROWS + 1][NUMDATA];
        try {
            Scanner input = new Scanner(file);
            int i = 0;

            while (input.hasNextLine()) {
                output[i] = input.nextLine().split(",");
                i++;
            }
            data = output;
        }
        catch (IOException e) {
            System.out.println("Error reading file");
        }
    }

    public static String[] getHeaders() {
        return data[0];
    }

    public static String[] getData(String select) {
        int saveSpot = 0, i = 0;
        String[] output = new String[NUMROWS];

        for (String header : getHeaders()) {
            if (select.toUpperCase().equals(header.toUpperCase())) {
                saveSpot = i;
            }
            else {
                i++;
            }
        }

        for (int x = 0; x < NUMROWS; x++) {
            output[x] = data[x+1][saveSpot];
        }

        return output;
    }
}