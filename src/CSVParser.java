import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by cjk98 on 11/20/2017.
 *
 * For EECS221 Term Project
 */

public class CSVParser {

    private String filePath = null;
    private String splitBy = ",";
    private LinkedList<Integer[]> result = new LinkedList<>();

    public CSVParser() {}

    public CSVParser (String filePath) {
        this.filePath = filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public LinkedList<Integer[]> getResult() {
        return result;
    }

    public void setSplitBy(String splitBy) {
        this.splitBy = splitBy;
    }

    public void printResult() {
        for (Integer[] iArray: result) {
            for (Integer i: iArray) {
                System.out.printf(String.format("%8d ", i));
            }
            System.out.printf("\n");
        }
    }

    public LinkedList<Integer[]> parse() {
        result = new LinkedList<>();
        if (this.filePath != null) {
            FileReaderWBuffer fb = new FileReaderWBuffer(this.filePath);
            // the first line is the attribute names, abandon
            fb.readLine();
            String line;
            while ((line = fb.readLine()) != null) {
                String[] splitLine = line.split(splitBy);
                ArrayList<Integer> splitNum = new ArrayList<>();
                for (String s: splitLine)
                    splitNum.add(Integer.valueOf(s));
                result.add(splitNum.toArray(new Integer[splitNum.size()]));
            }

            fb.close();
        }
        return result;
    }

    public static void main(String[] args){
        CSVParser parser = new CSVParser("F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\Data\\input_sample.csv");
        parser.parse();
        System.out.println(String.format("%8s %8s %8s", "Source", "Target", "Weight"));
        parser.printResult();
    }
}

