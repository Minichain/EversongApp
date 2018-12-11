package com.upf.minichain.eversongapp.enums;

public enum WindowFunctionEnum {
    RECTANGULAR_WINDOW(0),
    HANNING_WINDOW(1),
    HAMMING_WINDOW(2),
    BLACKMAN_WINDOW(3);

    private final int value;

    WindowFunctionEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
