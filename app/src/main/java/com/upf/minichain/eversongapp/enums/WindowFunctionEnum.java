package com.upf.minichain.eversongapp.enums;

public enum WindowFunctionEnum {
    RECTANGULAR_WINDOW,
    HANNING_WINDOW,
    HAMMING_WINDOW,
    BLACKMAN_WINDOW;

    public int getIntValue() {
        switch(this) {
            case RECTANGULAR_WINDOW:
                return 0;
            case HANNING_WINDOW:
                return 1;
            case HAMMING_WINDOW:
                return 2;
            case BLACKMAN_WINDOW:
            default:
                return 3;
        }
    }
}
