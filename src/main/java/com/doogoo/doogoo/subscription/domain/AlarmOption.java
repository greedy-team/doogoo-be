package com.doogoo.doogoo.subscription.domain;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;

public record AlarmOption(boolean enabled, Integer minutesBefore) {
    public AlarmOption {
        if (enabled) {
            if (minutesBefore == null) throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
            if (minutesBefore < 0) throw new DoogooException(ErrorCode.INVALID_TOKEN_FORMAT);
        }
    }
}
