package com.github.mcd.core.addon;

import com.google.gson.Gson;
import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class is majority taken from the Minestom project.
 * You can view the source code of this project here: https://github.com/Minestom/Minestom
 */
public class AddonManager {

    public final static Logger LOGGER = LoggerFactory.getLogger(AddonManager.class);

    private final static Gson GSON = new Gson();

    private final ServerProcess serverProcess;

    // LinkedHashMaps are HashMaps that preserve order
    private final Map<String, DungeonsAddon> addons = new LinkedHashMap<>();
    private final Map<String, DungeonsAddon> immutableAddons = Collections.unmodifiableMap(addons);

    private final Path addonFolder = Paths.get("addons/MCD/addons"); // todo we should probably pass this in
    private Path addonDataRoot = addonFolder;

    public AddonManager(ServerProcess serverProcess) {
        this.serverProcess = serverProcess;
    }

    @NotNull
    public Path getAddonFolder() {
        return addonFolder;
    }

    public @NotNull Path getAddonDataRoot() {
        return addonDataRoot;
    }

    public void setAddonDataRoot(@NotNull Path dataRoot) {
        this.addonDataRoot = dataRoot;
    }

    @NotNull
    public Collection<DungeonsAddon> getAddons() {
        return immutableAddons.values();
    }

    @Nullable
    public DungeonsAddon getAddon(@NotNull String name) {
        return addons.get(name.toLowerCase());
    }

    public boolean hasAddon(@NotNull String name) {
        return addons.containsKey(name);
    }

    //
    // Init phases
    //

    @ApiStatus.Internal
    public void start() {
        loadAddons();
    }

    //
    // Loading
    //

    private void loadAddons() {
        // Initialize folders
        try {
            // Make addons folder if necessary
            if (!Files.exists(addonFolder)) {
                Files.createDirectories(addonFolder);
            }
        } catch (IOException e) {
            LOGGER.error("Could not find nor create an addon folder, addons will not be loaded!");
            MinecraftServer.getExceptionManager().handleException(e);
            return;
        }

        // Load addons
        {
            // Get all addons and order them accordingly.
            List<DiscoveredAddon> discoveredAddons = discoverAddons();

            // Don't waste resources on doing extra actions if there is nothing to do.
            if (discoveredAddons.isEmpty()) return;

            // Create classloaders for each addon (so that they can be used during dependency resolution)
            Iterator<DiscoveredAddon> addonIterator = discoveredAddons.iterator();
            while (addonIterator.hasNext()) {
                DiscoveredAddon discoveredAddon = addonIterator.next();
                try {
                    discoveredAddon.createClassLoader();
                } catch (Exception e) {
                    discoveredAddon.loadStatus = DiscoveredAddon.LoadStatus.FAILED_TO_SETUP_CLASSLOADER;
                    serverProcess.exception().handleException(e);
                    LOGGER.error("Failed to load addon {}", discoveredAddon.getName());
                    LOGGER.error("Failed to load addon", e);
                    addonIterator.remove();
                }
            }

            // remove invalid addons
            discoveredAddons.removeIf(ext -> ext.loadStatus != DiscoveredAddon.LoadStatus.LOAD_SUCCESS);

            // Load the addons
            for (DiscoveredAddon discoveredAddon : discoveredAddons) {
                try {
                    loadAddon(discoveredAddon);
                } catch (Exception e) {
                    discoveredAddon.loadStatus = DiscoveredAddon.LoadStatus.LOAD_FAILED;
                    LOGGER.error("Failed to load addon {}", discoveredAddon.getName());
                    serverProcess.exception().handleException(e);
                }
            }
        }
    }

    public boolean loadDynamicAddon(@NotNull Path jarFile) throws FileNotFoundException {
        if (!Files.exists(jarFile)) {
            throw new FileNotFoundException("File '" + jarFile.toAbsolutePath() + "' does not exists. Cannot load addon.");
        }

        LOGGER.info("Discover dynamic addon from jar {}", jarFile.toAbsolutePath());
        DiscoveredAddon discoveredAddon = discoverFromJar(jarFile);
        List<DiscoveredAddon> addonsToLoad = Collections.singletonList(discoveredAddon);
        return loadAddonList(addonsToLoad);
    }

    /**
     * Loads an addons into Minestom.
     *
     * @param discoveredAddon The addon. Make sure to verify its integrity, set its class loader, and its files.
     * @return An addon object made from this DiscoveredAddon
     */
    @Nullable
    private DungeonsAddon loadAddon(@NotNull DiscoveredAddon discoveredAddon) {
        // Create addon (authors, version etc.)
        String addonName = discoveredAddon.getName();
        String mainClass = discoveredAddon.getEntrypoint();

        AddonClassLoader loader = discoveredAddon.getClassLoader();

        if (addons.containsKey(addonName.toLowerCase())) {
            LOGGER.error("An addon called '{}' has already been registered.", addonName);
            return null;
        }

        Class<?> jarClass;
        try {
            jarClass = Class.forName(mainClass, true, loader);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Could not find main class '{}' in addon '{}'.",
                mainClass, addonName, e);
            return null;
        }

        Class<? extends DungeonsAddon> addonClass;
        try {
            addonClass = jarClass.asSubclass(DungeonsAddon.class);
        } catch (ClassCastException e) {
            LOGGER.error("Main class '{}' in '{}' does not extend the 'DungeonsAddon' superclass.", mainClass, addonName, e);
            return null;
        }

        Constructor<? extends DungeonsAddon> constructor;
        try {
            constructor = addonClass.getDeclaredConstructor();
            // Let's just make it accessible, plugin creators don't have to make this public.
            constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Main class '{}' in '{}' does not define a no-args constructor.", mainClass, addonName, e);
            return null;
        }
        DungeonsAddon addon = null;
        try {
            addon = constructor.newInstance();
        } catch (InstantiationException e) {
            LOGGER.error("Main class '{}' in '{}' cannot be an abstract class.", mainClass, addonName, e);
            return null;
        } catch (IllegalAccessException ignored) {
            // We made it accessible, should not occur
        } catch (InvocationTargetException e) {
            LOGGER.error(
                "While instantiating the main class '{}' in '{}' an exception was thrown.",
                mainClass,
                addonName,
                e.getTargetException()
            );
            return null;
        }

        // Set addon origin to its DiscoveredAddon
        try {
            Field originField = DungeonsAddon.class.getDeclaredField("origin");
            originField.setAccessible(true);
            originField.set(addon, discoveredAddon);
        } catch (IllegalAccessException e) {
            // We made it accessible, should not occur
        } catch (NoSuchFieldException e) {
            LOGGER.error("Main class '{}' in '{}' has no description field.", mainClass, addonName, e);
            return null;
        }

        // Set logger
        try {
            Field loggerField = DungeonsAddon.class.getDeclaredField("logger");
            loggerField.setAccessible(true);
            loggerField.set(addon, LoggerFactory.getLogger(addonClass));
        } catch (IllegalAccessException e) {
            // We made it accessible, should not occur
            serverProcess.exception().handleException(e);
        } catch (NoSuchFieldException e) {
            // This should also not occur (unless someone changed the logger in Addon superclass).
            LOGGER.error("Main class '{}' in '{}' has no logger field.", mainClass, addonName, e);
        }

        // Set event node
        try {
            EventNode<Event> eventNode = EventNode.all(addonName); // Use the addon name
            Field loggerField = DungeonsAddon.class.getDeclaredField("eventNode");
            loggerField.setAccessible(true);
            loggerField.set(addon, eventNode);

            serverProcess.eventHandler().addChild(eventNode);
        } catch (IllegalAccessException e) {
            // We made it accessible, should not occur
            serverProcess.exception().handleException(e);
        } catch (NoSuchFieldException e) {
            // This should also not occur
            LOGGER.error("Main class '{}' in '{}' has no event node field.", mainClass, addonName, e);
        }

        // add to a linked hash map, as they preserve order
        addons.put(addonName.toLowerCase(), addon);

        return addon;
    }


    /**
     * Get all addons from the addons folder and make them discovered.
     * <p>
     * It skims the addon folder, discovers and verifies each addon, and returns those created DiscoveredAddons.
     *
     * @return A list of discovered addons from this folder.
     */
    private @NotNull List<DiscoveredAddon> discoverAddons() {
        List<DiscoveredAddon> addons = new LinkedList<>();

        // Attempt to find all the addons
        try {
            Files.list(addonFolder)
                .filter(file -> file != null &&
                    !Files.isDirectory(file) &&
                    file.getFileName().toString().endsWith(".jar"))
                .map(this::discoverFromJar)
                .filter(ext -> ext != null && ext.loadStatus == DiscoveredAddon.LoadStatus.LOAD_SUCCESS)
                .forEach(addons::add);
        } catch (IOException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        //TODO(mattw): Extract this into its own method to load an addon given classes and resources directory.
        //TODO(mattw): Should show a warning if one is set and not the other. It is most likely a mistake.

        return addons;
    }

    /**
     * Grabs a discovered addon from a jar.
     *
     * @param file The jar to grab it from (a .jar is a formatted .zip file)
     * @return The created DiscoveredAddon.
     */
    private @Nullable DiscoveredAddon discoverFromJar(@NotNull Path file) {
        try (ZipFile f = new ZipFile(file.toFile())) {

            ZipEntry entry = f.getEntry("addon.json");

            if (entry == null)
                throw new IllegalStateException("Missing addon.json in addon " + file.getFileName() + ".");

            InputStreamReader reader = new InputStreamReader(f.getInputStream(entry));

            // Initialize DiscoveredAddon from GSON.
            DiscoveredAddon addon = GSON.fromJson(reader, DiscoveredAddon.class);
            addon.setOriginalJar(file);
            addon.files.add(file.toUri().toURL());

            // Verify integrity and ensure defaults
            DiscoveredAddon.verifyIntegrity(addon);

            return addon;
        } catch (IOException e) {
            serverProcess.exception().handleException(e);
            return null;
        }
    }

    /**
     * Checks if this list of addons are loaded
     *
     * @param addons The list of addons to check against.
     * @return If all of these addons are loaded.
     */
    private boolean isLoaded(@NotNull List<DiscoveredAddon> addons) {
        return
            addons.isEmpty() // Don't waste CPU on checking an empty array
                // Make sure the internal addons list contains all of these.
                || addons.stream().allMatch(ext -> this.addons.containsKey(ext.getName().toLowerCase()));
    }

    private boolean loadAddonList(@NotNull List<DiscoveredAddon> addonsToLoad) {
        // setup new classloaders for the addons to reload
        for (DiscoveredAddon toReload : addonsToLoad) {
            LOGGER.debug("Setting up classloader for addon {}", toReload.getName());
//            toReload.setMinestomAddonClassLoader(toReload.makeClassLoader()); //TODO: Fix this
        }

        List<DungeonsAddon> newAddons = new LinkedList<>();
        for (DiscoveredAddon toReload : addonsToLoad) {
            // reload addons
            LOGGER.info("Actually load addons {}", toReload.getName());
            DungeonsAddon loadedAddon = loadAddon(toReload);
            if (loadedAddon != null) {
                newAddons.add(loadedAddon);
            }
        }

        if (newAddons.isEmpty()) {
            LOGGER.error("No addons to load, skipping callbacks");
            return false;
        }

        return true;
    }

    /**
     * Shutdowns all the addons by unloading them.
     */
    public void shutdown() {// copy names, as the addons map will be modified via the calls to unload
        Set<String> addonNames = new HashSet<>(addons.keySet());
        for (String addon : addonNames) {
            if (addons.containsKey(addon)) { // is still loaded? Because addons can depend on one another, it might have already been unloaded
                unloadAddon(addon);
            }
        }
    }

    private void unloadAddon(@NotNull String addonName) {
        DungeonsAddon addon = addons.get(addonName.toLowerCase());

        if (addon == null) {
            throw new IllegalArgumentException("Addon " + addonName + " is not currently loaded.");
        }

        LOGGER.info("Unloading addon {}", addonName);
        unload(addon);
    }

    private void unload(@NotNull DungeonsAddon addon) {
        // remove from loaded addons
        String id = addon.getOrigin().getName().toLowerCase();
        addons.remove(id);

        // cleanup classloader
        // TODO: Is it necessary to remove the CLs since this is only called on shutdown?
    }
}