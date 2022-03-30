package com.github.mcd.core.model.dice;

import java.util.concurrent.ThreadLocalRandom;

public record DiceCollection(int amount, int sides) {

    public int roll() {
        ThreadLocalRandom threadRandom = ThreadLocalRandom.current();
        int result = 0;
        for (int i = 0; i < this.amount; i++) {
            result += threadRandom.nextInt(1, this.sides + 1); // origin inclusive, bound exclusive
        }
        return result;
    }
}
