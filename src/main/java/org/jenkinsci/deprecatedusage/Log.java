package org.jenkinsci.deprecatedusage;

public final class Log {
    private Log() {
        super();
    }

    public static void log(String message) {
        System.out.println(message);
        System.out.flush();
    }

    public static void print(String message) {
        System.out.print(message);
        System.out.flush();
    }
}
