

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public enum FileUtil
{
    INSTANCE;

    public String read(String filename) throws IOException
    {
        StringBuilder strBuilder = new StringBuilder();
        List<String> lines = readLines(filename);
        for (String line : lines)
        {
            strBuilder.append(line + System.lineSeparator());
        }
        return strBuilder.toString();
    }

    public List<String> readLines(String filename) throws IOException
    {
        BufferedReader reader = new BufferedReader(
                new FileReader(new File(filename)));
        ArrayList<String> lines = new ArrayList<>();
        String line = null;
        while ((line = reader.readLine()) != null)
        {
            lines.add(line);
        }
        reader.close();
        return lines;
    }

    public void write(String filename, String content) throws IOException
    {
        BufferedWriter writer = new BufferedWriter(
                new FileWriter(new File(filename)));
        writer.write(content);
        writer.flush();
        writer.close();
    }

    public void writeLines(String filename, List<String> lines)
            throws IOException
    {
        StringBuilder strBuilder = new StringBuilder();
        for (String line : lines)
        {
            strBuilder.append(line + System.lineSeparator());
        }
        write(filename, strBuilder.toString());
    }
}
