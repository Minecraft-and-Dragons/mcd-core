package com.github.mcd.core.model.stats;

import com.github.mcd.core.model.language.Language;
import org.jetbrains.annotations.NotNull;

public interface LanguageProficiencyModifier {

    boolean isLanguageProficient(@NotNull Language language);
}
