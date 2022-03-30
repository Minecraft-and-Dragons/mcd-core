package com.github.mcd.core.model.language;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.NotNull;

// maybe we can do something fun with this in the future :)
@RequiredArgsConstructor
@Getter
public abstract class Language {
    protected final @NotNull NamespaceID identifier;
    protected final @NotNull String name;
}
