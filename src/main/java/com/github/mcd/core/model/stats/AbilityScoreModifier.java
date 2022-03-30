package com.github.mcd.core.model.stats;

import org.jetbrains.annotations.NotNull;

public interface AbilityScoreModifier {

    int getModifier(@NotNull AbilityScoreType abilityType);
}
