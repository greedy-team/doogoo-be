package com.doogoo.doogoo.subscription.domain;

import com.doogoo.doogoo.common.error.DoogooException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SubscriptionTest {

    @Test
    @DisplayName("token이 null이면 DoogooException")
    void constructor_throws_when_token_null() {
        assertThrows(DoogooException.class, () ->
                new Subscription(null, SourceType.ACADEMIC, "{}", false, null));
    }
}
