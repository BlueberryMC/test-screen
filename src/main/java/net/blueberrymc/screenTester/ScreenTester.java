package net.blueberrymc.screenTester;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import net.minecraft.CrashReport;
import net.minecraft.DetectedVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.SilentInitException;
import net.minecraft.server.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Function;

public class ScreenTester {
    public static boolean noItemRenderer = true;
    public static boolean noLevelRenderer = true;
    public static boolean noSplashManager = true;
    public static boolean noBlockEntityWithoutLevelRenderer = true;
    private static final Logger LOGGER = LogManager.getLogger();

    public static void launchMinecraft(boolean demo, @Nullable Function<Minecraft, Screen> toScreenFunction) {
        SharedConstants.tryDetectVersion();
        setBootstrapped();
        PerfTimer.push("Preload CrashReport");
        CrashReport.preload();
        PerfTimer.popPush("Render thread & Minecraft initialization");
        TestMinecraft minecraft;
        try {
            Thread.currentThread().setName("Render thread");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            minecraft = TestMinecraft.createMinecraft(demo);
            if (toScreenFunction != null) minecraft.setScreen(toScreenFunction.apply(minecraft));
            RenderSystem.finishInitialization();
        } catch (SilentInitException silentInitException) {
            LOGGER.warn("Failed to create window: ", silentInitException);
            return;
        } catch (Throwable ex) {
            CrashReport crashReport = CrashReport.forThrowable(ex, "Initializing game");
            crashReport.addCategory("Initialization");
            Minecraft.fillReport(null, null, Utils.VERSION, null, crashReport);
            Minecraft.crash(crashReport);
            return;
        }
        minecraft.options.guiScale = 2;
        PerfTimer.popPush("Game thread");
        Thread gameThread;
        if (minecraft.renderOnThread()) {
            gameThread = new Thread("Game thread") {
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        minecraft.run();
                    } catch (Throwable ex) {
                        LOGGER.error("Exception in client thread", ex);
                    }
                }
            };
            gameThread.start();
            //noinspection StatementWithEmptyBody
            while (minecraft.isRunning()); // wait forever until minecraft exits
        } else {
            gameThread = null;
            try {
                RenderSystem.initGameThread(false);
                minecraft.run();
            } catch (Throwable ex) {
                LOGGER.error("Unhandled game exception", ex);
            }
        }
        PerfTimer.popPush("Reset buffer uploader");
        BufferUploader.reset();
        PerfTimer.popPush("Stop game thread");
        try {
            minecraft.stop();
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException ex) {
            LOGGER.error("Exception during client thread shutdown", ex);
        } finally {
            try {
                System.setSecurityManager(new SecurityManagerExitTrap()); // prevents System.exit(0)
                minecraft.destroy();
            } catch (Utils.ExitTrappedException ignore) {
            } finally {
                System.setSecurityManager(null);
            }
            PerfTimer.pop();
        }
    }

    private static void setBootstrapped() {
        try {
            Field field = Bootstrap.class.getDeclaredField("isBootstrapped");
            field.setAccessible(true);
            field.set(null, true);
            Method wrapStreams = Bootstrap.class.getDeclaredMethod("wrapStreams");
            wrapStreams.setAccessible(true);
            wrapStreams.invoke(null);
        } catch (ReflectiveOperationException ignore) {
            Bootstrap.bootStrap();
        }
    }
}
