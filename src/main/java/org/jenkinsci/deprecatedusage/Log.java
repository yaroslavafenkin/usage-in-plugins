package org.jenkinsci.deprecatedusage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public final class Log {
    // relative to user dir
    private static final File LOG_FILE = new File("target/output.log");
    private static final Writer FILE_WRITER = openFileWriter();

    private Log() {
        super();
    }

    private static Writer openFileWriter() {
        try {
            return new FileWriter(LOG_FILE);
        } catch (final IOException e) {
            System.out.println("Unable to open " + LOG_FILE + " to write logs");
            return null;
        }
    }

    public static void log(String message) {
        System.out.println(message);
        System.out.flush();
        if (FILE_WRITER != null) {
            try {
                FILE_WRITER.write(message);
                FILE_WRITER.write('\n');
                FILE_WRITER.flush();
            } catch (final IOException e) {
                return;
            }
        }
    }

    public static void print(String message) {
        System.out.print(message);
        System.out.flush();
        if (FILE_WRITER != null) {
            try {
                FILE_WRITER.write(message);
                FILE_WRITER.flush();
            } catch (final IOException e) {
                return;
            }
        }
    }

    public static void closeLog() {
        try {
            FILE_WRITER.close();
        } catch (final IOException e) {
            return;
        }
    }
}
