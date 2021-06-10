package net.blueberrymc.screenTester;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Locale;

public class Utils {
    /**
     * Dummy version used to launch minecraft.
     */
    public static final String VERSION = "1.0.0.0";

    @Contract(" -> new")
    public static @NotNull File getMinecraftDir() {
        String userHomeDir = System.getProperty("user.home", ".");
        String osType = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        String mcDir = ".minecraft";
        if (osType.contains("win") && System.getenv("APPDATA") != null) {
            return new File(System.getenv("APPDATA"), mcDir);
        } else if (osType.contains("mac")) {
            return new File(new File(new File(userHomeDir, "Library"), "Application Support"), "minecraft");
        }
        return new File(userHomeDir, mcDir);
    }

    static class ExitTrappedException extends SecurityException {}
}
