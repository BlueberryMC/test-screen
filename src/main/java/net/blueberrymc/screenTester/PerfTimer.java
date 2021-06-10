package net.blueberrymc.screenTester;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class PerfTimer {
    private static final Logger LOGGER = LogManager.getLogger("PerfTimer");
    private static String current;
    private static long start;

    public static void push(@NotNull String s) {
        current = s;
        start = System.currentTimeMillis();
    }

    public static void pop() {
        long end = System.currentTimeMillis();
        log("'" + current + "' took " + (end - start) + " ms");
    }

    public static void popPush(@NotNull String s) {
        pop();
        push(s);
    }

    private static void log(String s) {
        LOGGER.info("[Performance Timer] " + s);
    }
}
