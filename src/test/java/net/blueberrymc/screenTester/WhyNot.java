package net.blueberrymc.screenTester;

import net.minecraft.client.gui.screens.LanguageSelectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.junit.Test;

public class WhyNot {
    @Test
    public void testLanguageSelectionScreen() {
        ScreenTester.launchMinecraft(false, mc -> new LanguageSelectScreen(new TitleScreen(), mc.options, mc.getLanguageManager()));
    }
}
