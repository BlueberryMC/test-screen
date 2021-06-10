package net.minecraft.server.packs.resources;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.blueberrymc.screenTester.ScreenTester;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.FoliageColorReloadListener;
import net.minecraft.client.resources.GrassColorReloadListener;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.PaintingTextureManager;
import net.minecraft.client.resources.SplashManager;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.searchtree.SearchRegistry;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.util.Unit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("unused")
public class SimpleReloadableResourceManager implements ReloadableResourceManager {
   private static final Logger LOGGER = LogManager.getLogger();
   private final Map<String, FallbackResourceManager> namespacedPacks = Maps.newHashMap();
   private final List<PreparableReloadListener> listeners = Lists.newArrayList();
   private final Set<String> namespaces = Sets.newLinkedHashSet();
   private final List<PackResources> packs = Lists.newArrayList();
   private final PackType type;

   public SimpleReloadableResourceManager(PackType packType) {
      this.type = packType;
   }

   public void add(PackResources packResources) {
      this.packs.add(packResources);

      for(String s : packResources.getNamespaces(this.type)) {
         this.namespaces.add(s);
         FallbackResourceManager fallbackResourceManager = this.namespacedPacks.get(s);
         if (fallbackResourceManager == null) {
            fallbackResourceManager = new FallbackResourceManager(this.type, s);
            this.namespacedPacks.put(s, fallbackResourceManager);
         }

         fallbackResourceManager.add(packResources);
      }
   }

   public Set<String> getNamespaces() {
      return this.namespaces;
   }

   public Resource getResource(ResourceLocation resourceLocation) throws IOException {
      ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
      if (resourceManager != null) {
         return resourceManager.getResource(resourceLocation);
      } else {
         throw new FileNotFoundException(resourceLocation.toString());
      }
   }

   public boolean hasResource(ResourceLocation resourceLocation) {
      ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
      return resourceManager != null && resourceManager.hasResource(resourceLocation);
   }

   public List<Resource> getResources(ResourceLocation resourceLocation) throws IOException {
      ResourceManager resourceManager = this.namespacedPacks.get(resourceLocation.getNamespace());
      if (resourceManager != null) {
         return resourceManager.getResources(resourceLocation);
      } else {
         throw new FileNotFoundException(resourceLocation.toString());
      }
   }

   public Collection<ResourceLocation> listResources(String s, Predicate<String> predicate) {
      Set<ResourceLocation> set = Sets.newHashSet();

      for(FallbackResourceManager fallbackResourceManager : this.namespacedPacks.values()) {
         set.addAll(fallbackResourceManager.listResources(s, predicate));
      }

      List<ResourceLocation> list = Lists.newArrayList(set);
      Collections.sort(list);
      return list;
   }

   private void clear() {
      this.namespacedPacks.clear();
      this.namespaces.clear();
      this.packs.forEach(PackResources::close);
      this.packs.clear();
   }

   public void close() {
      this.clear();
   }

   public void registerReloadListener(PreparableReloadListener preparableReloadListener) {
      if (preparableReloadListener instanceof GrassColorReloadListener ||
              preparableReloadListener instanceof FoliageColorReloadListener ||
              preparableReloadListener instanceof SoundManager ||
              preparableReloadListener instanceof ParticleEngine ||
              (ScreenTester.noItemRenderer && preparableReloadListener instanceof ItemRenderer) ||
              (ScreenTester.noLevelRenderer && preparableReloadListener instanceof LevelRenderer) ||
              (ScreenTester.noSplashManager && preparableReloadListener instanceof SplashManager) ||
              (ScreenTester.noBlockEntityWithoutLevelRenderer && preparableReloadListener instanceof BlockEntityWithoutLevelRenderer) ||
              preparableReloadListener instanceof SearchRegistry ||
              preparableReloadListener instanceof PaintingTextureManager ||
              preparableReloadListener instanceof MobEffectTextureManager ||
              preparableReloadListener instanceof GameRenderer ||
              preparableReloadListener instanceof BlockEntityRenderDispatcher) return;
      this.listeners.add(preparableReloadListener);
   }

   public ReloadInstance createReload(Executor executor, Executor executor2, CompletableFuture<Unit> completableFuture, List<PackResources> list) {
      LOGGER.info("Reloading ResourceManager: {}", () -> list.stream().map(PackResources::getName).collect(Collectors.joining(", ")));
      this.clear();

      for(PackResources packResources : list) {
         try {
            this.add(packResources);
         } catch (Exception var8) {
            LOGGER.error("Failed to add resource pack {}", packResources.getName(), var8);
            return new FailingReloadInstance(new ResourcePackLoadingFailure(packResources, var8));
         }
      }

      return LOGGER.isDebugEnabled() ? new ProfiledReloadInstance(this, Lists.newArrayList(this.listeners), executor, executor2, completableFuture) : SimpleReloadInstance.of(this, Lists.newArrayList(this.listeners), executor, executor2, completableFuture);
   }

   public Stream<PackResources> listPacks() {
      return this.packs.stream();
   }

   static class FailingReloadInstance implements ReloadInstance {
      private final ResourcePackLoadingFailure exception;
      private final CompletableFuture<Unit> failedFuture;

      public FailingReloadInstance(ResourcePackLoadingFailure resourcePackLoadingFailure) {
         this.exception = resourcePackLoadingFailure;
         this.failedFuture = new CompletableFuture<>();
         this.failedFuture.completeExceptionally(resourcePackLoadingFailure);
      }

      public CompletableFuture<Unit> done() {
         return this.failedFuture;
      }

      public float getActualProgress() {
         return 0.0F;
      }

      public boolean isDone() {
         return true;
      }

      public void checkExceptions() {
         throw this.exception;
      }
   }

   public static class ResourcePackLoadingFailure extends RuntimeException {
      private final PackResources pack;

      public ResourcePackLoadingFailure(PackResources packResources, Throwable throwable) {
         super(packResources.getName(), throwable);
         this.pack = packResources;
      }

      public PackResources getPack() {
         return this.pack;
      }
   }
}