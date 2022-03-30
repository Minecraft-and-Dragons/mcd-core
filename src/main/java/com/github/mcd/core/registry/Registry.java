package com.github.mcd.core.registry;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Registry<T, ID> {
    private final Map<? extends ID, ? extends T> store = new ConcurrentHashMap<>();

    public @Nullable T getById(ID id) {
        return this.store.get(id);
    }
}
