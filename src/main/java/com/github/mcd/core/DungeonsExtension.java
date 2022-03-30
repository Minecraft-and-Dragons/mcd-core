package com.github.mcd.core;

import com.github.mcd.core.addon.AddonManager;
import com.github.mcd.core.addon.DungeonsAddon;
import net.minestom.server.MinecraftServer;
import net.minestom.server.extensions.Extension;
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
    }

    @Override
    public void terminate() {

    }
}
