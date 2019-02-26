package com.upf.minichain.eversongapp.enums;

public enum EversongFunctionalities {
    CHORD_DETECTION(0),
    CHORD_SCORE(1),
    TUNING(2);

    private final int value;

    EversongFunctionalities(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
