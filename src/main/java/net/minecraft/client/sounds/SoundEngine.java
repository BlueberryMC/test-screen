package net.minecraft.client.sounds;

import net.minecraft.client.Camera;
import net.minecraft.client.Options;
import net.minecraft.client.resources.sounds.Sound;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;

/**
 * Empty class (with required methods) to disable in-game sounds (which doesn't work)
 */
@SuppressWarnings("unused")
public class SoundEngine {
    public SoundEngine(SoundManager soundManager, Options options, ResourceManager resourceManager) {}
    public void reload() {}
    public void loadLibrary() {}
    public void updateCategoryVolume(SoundSource soundSource, float f) {}
    public void destroy() {}
    public void stop(SoundInstance soundInstance) {}
    public void stopAll() {}
    public void addEventListener(SoundEventListener soundEventListener) {}
    public void removeEventListener(SoundEventListener soundEventListener) {}
    public void tick(boolean flag) {}
    public boolean isActive(SoundInstance soundInstance) { return false; }
    public void play(SoundInstance soundInstance) {}
    public void queueTickingSound(TickableSoundInstance tickableSoundInstance) {}
    public void requestPreload(Sound sound) {}
    public void pause() {}
    public void resume() {}
    public void playDelayed(SoundInstance soundInstance, int i) {}
    public void updateSource(Camera camera) {}
    public void stop(ResourceLocation resourceLocation, SoundSource soundSource) {}
    public String getDebugString() { return "Sounds: 0/0 + 0/0"; }
}
