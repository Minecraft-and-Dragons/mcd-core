package com.github.mcd.core.model.spell;

import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public abstract class Spell {
    protected final String name;
    protected final String description;
    protected final int level;
    protected final Set<SpellComponent> components;
    protected final SpellSchool spellSchool;

}
