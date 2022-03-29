package com.github.mcd.core.model.stats;

import lombok.Getter;
import com.github.mcd.core.model.Character;

import java.util.Set;

@Getter
public class AbilityScore {
    private final int value;
    private final Set<SkillScoreType> proficientSkills;

    public AbilityScore(int value, Set<SkillScoreType> proficientSkills) {
        this.value = value;
        this.proficientSkills = proficientSkills;
    }

    public int getSkillScore(SkillScoreType type, Character character) {
        return this.value + (this.proficientSkills.contains(type) ? character.getProficiencyBonus() : 0);
    }
}
