package com.upf.minichain.eversongapp.enums;

public enum ChordDetectionAlgorithm {
    ADAM_STARK_ALGORITHM(0),
    EVERSONG_ALGORITHM(1);

    private final int value;

    ChordDetectionAlgorithm(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
