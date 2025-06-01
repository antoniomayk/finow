package dev.antoniomayk.finow.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TestStringUtils {
  @Test
  void shouldChangeToSingleLineString() {
    var multilineString =
        """
        HELLO
        WORLD
        """;

    Assertions.assertEquals("HELLO WORLD", StringUtils.makeSingleLine(multilineString));
  }
}
