package net.blueberrymc.screenTester;

import com.mojang.authlib.properties.PropertyMap;
import com.mojang.blaze3d.platform.DisplayData;
import net.minecraft.client.User;
import net.minecraft.client.main.GameConfig;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.util.OptionalInt;
import java.util.UUID;

public class GameConfigHelper {
    @Contract("_ -> new")
    public static @NotNull GameConfig createGameConfig(boolean demo) {
        return new GameConfig(createUserData(), createDisplayData(), createFolderData(), createGameData(demo), createEmptyServerData());
    }

    @Contract(" -> new")
    public static @NotNull User createDummyUser() {
        return new User("Dummy", UUID.randomUUID().toString(), "", "mojang");
    }

    @Contract(" -> new")
    public static @NotNull PropertyMap createEmptyPropertyMap() {
        return new PropertyMap();
    }

    @Contract(" -> new")
    public static @NotNull GameConfig.UserData createUserData() {
        return new GameConfig.UserData(createDummyUser(), createEmptyPropertyMap(), createEmptyPropertyMap(), Proxy.NO_PROXY);
    }

    @Contract(" -> new")
    public static @NotNull DisplayData createDisplayData() {
        return new DisplayData(854, 480, OptionalInt.empty(), OptionalInt.empty(), false);
    }

    @Contract(" -> new")
    public static @NotNull GameConfig.FolderData createFolderData() {
        try {
            File tmpDir = Files.createTempDirectory("minecraft-blueberry-test-screen-").toFile();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    FileUtils.deleteDirectory(tmpDir);
                } catch (IOException e) {
                    System.err.println("Failed to delete directory: " + tmpDir.getAbsolutePath());
                    e.printStackTrace();
                }
            }));
            File resourcePacks = new File(tmpDir, "resourcepacks");
            //noinspection ResultOfMethodCallIgnored
            resourcePacks.mkdirs();
            return new GameConfig.FolderData(tmpDir, resourcePacks, new File(Utils.getMinecraftDir(), "assets"), null);
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    @Contract("_ -> new")
    public static @NotNull GameConfig.GameData createGameData(boolean demo) {
        return new GameConfig.GameData(demo, Utils.VERSION, "test", false, false);
    }

    @Contract(value = " -> new", pure = true)
    public static @NotNull GameConfig.ServerData createEmptyServerData() {
        return new GameConfig.ServerData(null, 25565);
    }
}
