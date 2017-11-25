import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by cjk98 on 1/21/2017.
 * for convenience
 */
public class FileReaderWBuffer {
    private File thisFile = null;
    private FileReader fr = null;
    private BufferedReader br = null;
    private String filePath = null;

    public FileReaderWBuffer(String filePath){
        this.filePath = filePath;
        thisFile = new File(filePath);
        try {
            fr = new FileReader(thisFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        br = new BufferedReader(fr);
    }

    public String readFileToString(Charset encoding) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, encoding);
    }

    // read a line
    public String readLine() {
        try {
            return br.readLine();
        } catch (IOException e) {
            System.out.println("readline() fail for BufferedReader!");
            e.printStackTrace();
        }
        return null;
    }

    // read a line
    public ArrayList<String> readAll() {
        try {
            ArrayList<String> resultList = new ArrayList<>();
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                resultList.add(sCurrentLine);
            }
            return resultList;
        } catch (IOException e) {
            System.out.println("readAll() fail for BufferedReader!");
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        try {
            if (br != null && fr != null)
            br.close();
        } catch (IOException e) {
            System.out.println("BufferedReader close failed!");
            e.printStackTrace();
        }
    }

    public long getFileSize() {
        return thisFile.length();
    }

}
