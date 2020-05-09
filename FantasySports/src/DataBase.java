import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class DataBase {
    private static File file = new File("FantasySports/database.csv");
    private static String[][] data;
    private static final int NUMROWS = 32;
    private static final int NUMDATA = 4;

    public static void addFields() {
        try {
            String[][] rows = new String[NUMROWS][NUMDATA];
            //                      Name   Offense   Defense   Support
            rows[0] = new String[] {"Ana", "30", "20", "85"};
            rows[1] = new String[] {"Ashe", "85", "5", "05"};
            rows[2] = new String[] {"Baptiste", "20", "50", "80"};
            rows[3] = new String[] {"Bastion", "50", "20", "5"};
            rows[4] = new String[] {"Brigitte", "35", "40", "75"};
            rows[5] = new String[] {"D.VA", "60", "60", "15"};
            rows[6] = new String[] {"Doomfist", "80", "5", "0"};
            rows[7] = new String[] {"Echo", "75", "25", "10"};
            rows[8] = new String[] {"Genji", "90", "5", "0"};
            rows[9] = new String[] {"Hanzo", "85", "10", "0"};
            rows[10] = new String[] {"Junkrat", "90", "15", "5"};
            rows[11] = new String[] {"Lucio", "15", "25", "80"};
            rows[12] = new String[] {"McCree", "90", "10", "0"};
            rows[13] = new String[] {"Mei", "40", "65", "10"};
            rows[14] = new String[] {"Mercy", "5", "20", "100"};
            rows[15] = new String[] {"Moira", "55", "15", "75"};
            rows[16] = new String[] {"Orisa", "30", "85", "10"};
            rows[17] = new String[] {"Pharah", "85", "5", "0"};
            rows[18] = new String[] {"Reaper", "100", "0", "0"};
            rows[19] = new String[] {"Reinhardt", "20", "100", "0"};
            rows[20] = new String[] {"Roadhog", "50", "50", "0"};
            rows[21] = new String[] {"Sigma", "35", "60", "10"};
            rows[22] = new String[] {"Soldier: 76", "95", "5", "15"};
            rows[23] = new String[] {"Sombra", "90", "0", "10"};
            rows[24] = new String[] {"Symmetra", "45", "60", "15"};
            rows[25] = new String[] {"Torbjorn", "45", "60", "25"};
            rows[26] = new String[] {"Tracer", "95", "0", "0"};
            rows[27] = new String[] {"Widowmaker", "85", "10", "10"};
            rows[28] = new String[] {"Winston", "40", "50", "0"};
            rows[29] = new String[] {"Wrecking Ball", "65", "25", "0"};
            rows[30] = new String[] {"Zarya", "50", "20", "25"};
            rows[31] = new String[] {"Zenyatta", "45", "10", "25"};

            FileWriter output = new FileWriter(file);
            output.append("Name");
            output.append(",");
            output.append("Offense");
            output.append(",");
            output.append("Defense");
            output.append(",");
            output.append("Support");
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

    public static String[] getStats(String name) {
        readFile();
        for (int i = 0; i < NUMROWS; i++) {
            if (data[i][0].toUpperCase().equals(name.toUpperCase())) {
                return data[i];
            }
        }
        return new String[] {""};
    }
}