package ru.job4j.grabber.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HabrCareerDateTimeParserTest {
    @Test
    public void whenParseOffsetTimeThenCorrect() {
        DateTimeParser parser = new HabrCareerDateTimeParser();
        String parseTime = "2024-11-28T12:27:09+03:00";
        String expected = "2024-11-28T12:27:09";
        assertThat(parser.parse(parseTime).toString()).isEqualTo(expected);
    }
}