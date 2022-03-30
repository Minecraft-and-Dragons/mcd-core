package com.github.mcd.core.model.race;

import com.github.mcd.core.model.DamageType;
import com.github.mcd.core.model.language.Language;
import com.github.mcd.core.model.stats.AbilityScoreModifier;
import com.github.mcd.core.model.stats.AbilityScoreType;
import com.github.mcd.core.model.stats.LanguageProficiencyModifier;
import com.github.mcd.core.model.stats.ResistanceModifier;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Set;

 // todo dont use in the future, along with @Getter, @Setter, etc..
@RequiredArgsConstructor
public abstract class Race implements AbilityScoreModifier, ResistanceModifier, LanguageProficiencyModifier {
    protected final String name;
    protected final Set<DamageType> resistances;
    protected final Set<Language> languages;
    protected final Map<AbilityScoreType, Integer> abilityScoreModifiers;
    protected final int hpModifier;
    // todo some races have features such as being proficient in medium and light armour. This must be supported

     @Override
    public int getModifier(@NotNull AbilityScoreType abilityType) {
        Integer value = this.abilityScoreModifiers.get(abilityType);
        return value == null ? 0 : value;
    }

     @Override
     public boolean isResistant(@NotNull DamageType damageType) {
         return this.resistances.contains(damageType);
     }

     @Override
     public boolean isLanguageProficient(@NotNull Language language) {
         return this.languages.contains(language);
     }
 }
