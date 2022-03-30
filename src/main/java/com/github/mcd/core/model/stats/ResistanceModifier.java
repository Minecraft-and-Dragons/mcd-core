package com.github.mcd.core.model.stats;

import com.github.mcd.core.model.DamageType;
import org.jetbrains.annotations.NotNull;

public interface ResistanceModifier {

    boolean isResistant(@NotNull DamageType damageType);
}
