package com.upf.minichain.eversongapp.enums;

public enum BroadcastMessage {
    REFRESH_FRAME;

    public String toString() {
        switch(this) {
            case REFRESH_FRAME:
                return "REFRESH_FRAME";
            default:
                return "";
        }
    }
}
