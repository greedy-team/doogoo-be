package com.doogoo.doogoo.subscription.domain;

public record AlarmOption(boolean enabled, Integer minutesBefore) {
    public AlarmOption {
        if (enabled) {
            if (minutesBefore == null) throw new IllegalArgumentException();
            if (minutesBefore < 0) throw new IllegalArgumentException();
        }
    }
}
