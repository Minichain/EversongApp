package com.upf.minichain.eversongapp.enums;

public enum TabSelected {
    GUITAR_TAB(0),
    UKULELE_TAB(1),
    PIANO_TAB(2),
    STAFF_TAB(3),
    CHROMAGRAM(4);

    private final int value;

    TabSelected(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}