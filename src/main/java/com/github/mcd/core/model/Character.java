package com.github.mcd.core.model;

import com.github.mcd.core.model.stats.AbilityScore;
import com.github.mcd.core.model.stats.AbilityScoreType;
import lombok.Getter;

import java.util.Map;

@Getter
public class Character {
    private String name;
    private String race;
    private String playerClass;
    private int level;

    private Alignment alignment;

    private int proficiencyBonus; // todo could be located from
    private int passiveWisdom; // todo should we just calculate this?
    private Map<AbilityScoreType, AbilityScore> abilityScores;

    private int armourClass;
    private int initiativeBonus;
    private int speed;

    private int maxHitPoints;
    private int hitPoints;
    private int temporaryHitPoints;

    private boolean inspiration;
}
