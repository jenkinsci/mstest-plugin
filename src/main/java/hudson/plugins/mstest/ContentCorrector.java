package hudson.plugins.mstest;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ivo on 27/03/2015.
 */
public class ContentCorrector
{
    private String file;

    public ContentCorrector(String file)
    {
        this.file = file;
    }

    public String fix() throws IOException
    {
        String filename = Integer.toString(randInt(1000, 1000000)) + ".rnd";
        File inFile = new File(file);
        File parent = inFile.getParentFile();
        File outfile = new File(parent, filename);
        PrintWriter out = new PrintWriter(outfile);
        BufferedReader in = new BufferedReader(new FileReader(inFile));
        String line = in.readLine();
        while (line != null) {
            line = stripIllegalEntities(stripIllegalCharacters(line));
            out.println(line);
            line = in.readLine();
        }
        in.close();
        out.close();
        return outfile.getAbsolutePath();
    }

    private String stripIllegalCharacters(String line)
    {
        String xml10pattern = "[^"
                + "\u0009\r\n"
                + "\u0020-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]";
        String xml11pattern = "[^"
                + "\u0001-\uD7FF"
                + "\uE000-\uFFFD"
                + "\ud800\udc00-\udbff\udfff"
                + "]+";
        return line.replaceAll(xml10pattern, "").replaceAll(xml11pattern, "");
    }

    private String stripIllegalEntities(String line)
    {
        final String pattern = "(?<entity>&#x(?<char>[0-9A-Ca-c]{1,4});)";
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(line);

        while (m.find()) {
            long c = Long.parseLong(m.group("char"), 16);
            if (!isAllowed(c)) {
                line = new StringBuilder(line).replace(m.start(1), m.end(1), "").toString();
                m = p.matcher(line);
            }
        }
        return line;
    }

    public boolean isAllowed(long c)
    {
        return c == 9 || c == 0xA || c == 0xD || (c > 0x20 && c < 0xD7FF) || (c > 0xE000 && c < 0xFFFD) || (c > 0x10000 && c < 0x10FFFF);
    }

    public static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }
}
