package hudson.plugins.mstest;

import com.google.common.base.Charsets;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nilleb
 */
class ContentCorrector {

    private static final String ILLEGAL_ENTITIES_PATTERN = "(&#x([0-9A-Fa-f]{1,4});)";

    private String file;

    ContentCorrector(String file) {
        this.file = file;
    }

    private static int randInt(int min, int max) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    void fix() throws IOException {
        String filename = Integer.toString(randInt(1000, 1000000)) + ".trx";
        File inFile = new File(file);
        File parent = inFile.getParentFile();
        File outfile = new File(parent, filename);
        boolean replace = false;

        try (PrintWriter out = new PrintWriter(
            new OutputStreamWriter(new FileOutputStream(outfile), Charsets.UTF_8))) {
            try (BufferedReader in = new BufferedReader(
                new InputStreamReader(new FileInputStream(inFile), Charsets.UTF_8))) {
                String line = in.readLine();
                while (line != null) {
                    String newline = stripIllegalEntities(line);
                    if (line.length() != newline.length()) {
                        replace = true;
                    }
                    out.println(newline);
                    line = in.readLine();
                }
            }
        }
        MsTestLogger logger = new MsTestLogger(null);

        if (replace) {
            FileOperator.safeDelete(inFile, logger);
            boolean success = outfile.renameTo(inFile);
            if (!success) {
                logger.error("Unable to move the file %s to %s.", outfile.getAbsolutePath(),
                    inFile.getAbsolutePath());
            }
        } else {
            FileOperator.safeDelete(outfile, logger);
        }
    }

    private String stripIllegalEntities(String line) {
        Pattern p = Pattern.compile(ILLEGAL_ENTITIES_PATTERN);
        Matcher m = p.matcher(line);
        String replaced = line;

        while (m.find()) {
            String charGroup = m.group(2);
            long c = Long.parseLong(charGroup, 16);
            if (!isAllowed(c)) {
                replaced = new StringBuilder(replaced).replace(m.start(1), m.end(1), "").toString();
                m = p.matcher(replaced);
            }
        }
        return replaced;
    }

    private boolean isAllowed(long c) {
        return c == 9 || c == 0xA || c == 0xD || (c > 0x20 && c < 0xD7FF) || (c > 0xE000
            && c < 0xFFFD) || (c > 0x10000 && c < 0x10FFFF);
    }
}
