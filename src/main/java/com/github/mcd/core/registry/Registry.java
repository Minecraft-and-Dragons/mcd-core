package com.github.mcd.core.registry;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Registry<T, ID> {
    private final Map<ID, T> store = new ConcurrentHashMap<>();

    public @Nullable T getById(ID id) {
        return this.store.get(id);
    }

    public void put(@NotNull ID id, @NotNull T value) {
        this.store.put(id, value);
    }
}
