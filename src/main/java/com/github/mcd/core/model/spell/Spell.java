package com.github.mcd.core.model.spell;

import java.util.Set;

public abstract class Spell {
    protected final String name;
    protected final String description;
    protected final int level;
    protected final Set<SpellComponent> components;
    protected final SpellSchool spellSchool;

}
