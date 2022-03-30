package com.github.mcd.core;

import com.github.mcd.core.addon.AddonManager;
import com.github.mcd.core.addon.DungeonsAddon;
import com.github.mcd.core.model.language.Language;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.Store;
import org.reflections.scanners.Scanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class DungeonsExtension extends Extension {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonsExtension.class);

    private final AddonManager addonManager = new AddonManager(MinecraftServer.process());

    @Override
    public void initialize() {
        LOGGER.info("Loading Dungeons addons");
        this.addonManager.start();
        Collection<DungeonsAddon> addons = this.addonManager.getAddons();
        LOGGER.info("Finished loading Dungeons addons. Found {}", addons.size());

        addons.forEach(addon -> {
            new Reflections(
                new ConfigurationBuilder()
                    .forPackage(addon.getOrigin().getScannedPackage(), addon.getOrigin().getClassLoader())
            ).getAllTypes().forEach(clazz -> System.out.println("Language class found: " + clazz));
            new Reflections(addon.getOrigin().getScannedPackage())
                .getSubTypesOf(Language.class)
                .forEach(clazz -> System.out.println("Language class found: " + clazz));
            System.out.println(new Reflections(addon.getOrigin().getScannedPackage())
                .getAllTypes());
        });
    }

    @Override
    public void terminate() {

    }
}
