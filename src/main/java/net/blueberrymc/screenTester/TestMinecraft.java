package net.blueberrymc.screenTester;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.main.GameConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TestMinecraft extends Minecraft {
    private static int loadingOverlayCount = 0;

    public TestMinecraft(@NotNull GameConfig gameConfig) {
        super(gameConfig);
    }

    @Contract("_ -> new")
    public static @NotNull TestMinecraft createMinecraft(boolean demo) {
        return new TestMinecraft(GameConfigHelper.createGameConfig(demo));
    }

    @Override
    public void setOverlay(@Nullable Overlay overlay) {
        if (!(overlay instanceof LoadingOverlay) || loadingOverlayCount++ == 0) {
            // if overlay is not LoadingOverlay or LoadingOverlay but loadingOverlayCount++ is 0
            super.setOverlay(overlay);
        }
    }
}
