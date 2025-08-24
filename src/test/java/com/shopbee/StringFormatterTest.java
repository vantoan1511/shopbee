package com.shopbee;

import com.shopbee.common.StringFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class StringFormatterTest {

    @Test
    void format() {
        assertAll(
                () -> assertEquals("Hello, World!", StringFormatter.format("Hello, {}!", "World")),
                () -> assertEquals("{}", StringFormatter.format("{}")),
                () -> assertEquals("Value: 42", StringFormatter.format("Value: {}", 42)),
                () -> assertEquals("Value: 42.0", StringFormatter.format("Value: {}", 42.0)),
                () -> assertEquals("Value: NaN", StringFormatter.format("Value: {}", Double.NaN)),
                () -> assertEquals("No placeholders here.", StringFormatter.format("No placeholders here.")),
                () -> assertEquals("Multiple: One, Two, Three", StringFormatter.format("Multiple: {}, {}, {}", "One", "Two", "Three")),
                () -> assertEquals("Incomplete: One, Two, {}", StringFormatter.format("Incomplete: {}, {}, {}", "One", "Two")),
                () -> assertNull(StringFormatter.format(null, "Test")),
                () -> assertEquals("Null parameter: null", StringFormatter.format("Null parameter: {}", (Object) null))
        );
    }
}