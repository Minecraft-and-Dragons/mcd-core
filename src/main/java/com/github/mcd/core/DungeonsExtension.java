package com.github.mcd.core;

import com.github.mcd.core.addon.AddonManager;
import com.github.mcd.core.addon.DungeonsAddon;
import com.github.mcd.core.model.language.Language;
import com.github.mcd.core.registry.types.LanguageRegistry;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Set;

public class DungeonsExtension extends Extension {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonsExtension.class);

    private final AddonManager addonManager = new AddonManager(MinecraftServer.process());

    private final LanguageRegistry languageRegistry = new LanguageRegistry();

    @Override
    public void initialize() {
        LOGGER.info("Loading Dungeons addons");
        this.addonManager.start();
        Collection<DungeonsAddon> addons = this.addonManager.getAddons();

        this.loadDataFromAddons(addons);
    }

    private void loadDataFromAddons(@NotNull Collection<DungeonsAddon> addons) {
        for (DungeonsAddon addon : addons) {
            LOGGER.info("---- [ Loading Addon {} ] ----", addon.getOrigin().getName());
            Reflections reflections = new Reflections(
                new ConfigurationBuilder()
                    .forPackage(addon.getOrigin().getScannedPackage(), addon.getOrigin().getClassLoader())
            );
            this.loadLanguagesFromAddon(reflections);
            LOGGER.info("---- [ Finished Loading Addon ] ----");
        }
    }

    private void loadLanguagesFromAddon(@NotNull Reflections reflections) {
        Set<Class<? extends Language>> clazzes = reflections.getSubTypesOf(Language.class);

        for (Class<? extends Language> clazz : clazzes) {
            try {
                Language language = clazz.getDeclaredConstructor().newInstance();
                this.languageRegistry.put(language.getIdentifier(), language);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                LOGGER.error("Error instantiating Language ({}): {}", clazz, ex);
            }
        }
        LOGGER.info("Loaded {} languages", clazzes.size());
    }

    @Override
    public void terminate() {

    }
}
