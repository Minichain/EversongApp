package com.upf.minichain.eversongapp.enums;

public enum ChordDetectionAlgorithm {
    ADAM_STARK_ALGORITHM(0),
    EVERSONG_ALGORITHM_1(1),
    EVERSONG_ALGORITHM_2(2);

    private final int value;

    ChordDetectionAlgorithm(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
