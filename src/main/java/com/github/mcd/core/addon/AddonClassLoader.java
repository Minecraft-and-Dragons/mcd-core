package com.github.mcd.core.addon;

import com.github.mcd.core.DungeonsExtension;
import net.minestom.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is majority taken from the Minestom project.
 * You can view the source code of this project here: https://github.com/Minestom/Minestom
 */
public final class AddonClassLoader extends URLClassLoader {

    public AddonClassLoader(String name, URL[] urls) {
        super("MCD_Add_" + name, urls, DungeonsExtension.class.getClassLoader());
    }

    public AddonClassLoader(String name, URL[] urls, ClassLoader parent) {
        super("Ext_" + name, urls, parent);
    }

    @Override
    public void addURL(@NotNull URL url) {
        super.addURL(url);
    }


    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }
}