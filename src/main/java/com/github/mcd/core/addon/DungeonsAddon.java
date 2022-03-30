package com.github.mcd.core.addon;

import com.github.mcd.core.model.race.Race;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

/**
 * This class is majority taken from the Minestom project.
 * You can view the source code of this project here: https://github.com/Minestom/Minestom
 */
public abstract class DungeonsAddon {
    private static final Logger LOGGER = LoggerFactory.getLogger(DungeonsAddon.class);
    // Set by reflection
    @SuppressWarnings("unused")
    private DiscoveredAddon origin;

    protected DungeonsAddon() {

    }

    @NotNull
    public DiscoveredAddon getOrigin() {
        return origin;
    }

    /**
     * Gets a resource from inside the addon jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param fileName The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getPackagedResource(@NotNull String fileName) {
        try {
            final URL url = getOrigin().getClassLoader().getResource(fileName);
            if (url == null) {
                LOGGER.debug("Resource not found: {}", fileName);
                return null;
            }

            return url.openConnection().getInputStream();
        } catch (IOException ex) {
            LOGGER.debug("Failed to load resource {}.", fileName, ex);
            return null;
        }
    }

    /**
     * Gets a resource from inside the addon jar.
     * <p>
     * The caller is responsible for closing the returned {@link InputStream}.
     *
     * @param target The file to read
     * @return The file contents, or null if there was an issue reading the file.
     */
    public @Nullable InputStream getPackagedResource(@NotNull Path target) {
        return getPackagedResource(target.toString().replace('\\', '/'));
    }

    public Set<Race> getAddedRaces() {
        return new HashSet<>();
    }
}