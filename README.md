# test-screen

A small Minecraft to test screens quickly without starting a full Minecraft instance.

## How to use
It's very simple! <small>(assuming you already have minecraft as dependency)</small>

<small>(`src/main/test/java/net/blueberrymc/screenTester/WhyNot.java`)</small>
```java
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
```
When you execute the test, it will run the Minecraft and opens LanguageSelectScreen.

## Limitations
Since this library is intended to test screens, these features are unavailable:
- Interacting with world
- Particles
- Sound engine (so the game will be silent at all)
- Painting texture
- Mob effect texture
- Search registry
- Grass color
- Foliage color
