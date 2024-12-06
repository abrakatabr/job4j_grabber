package ru.job4j.grabber.service;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.utils.DateTimeParser;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class HabrCareerParse implements Parse {

    private static final Logger LOG = Logger.getLogger(HabrCareerParse.class);
    private static final String SOURCE_LINK = "https://career.habr.com";
    private static final String PREFIX = "/vacancies?page=";
    private static final String SUFFIX = "&q=Java%20developer&type=all";
    private static final int PAGES_COUNT = 5;
    private final DateTimeParser dateTimeParser;

    public HabrCareerParse(DateTimeParser dateTimeParser) {
        this.dateTimeParser = dateTimeParser;
    }

    @Override
    public List<Post> fetch() {
        var result = new ArrayList<Post>();
        try {
            for (int pageNumber = 1; pageNumber <= PAGES_COUNT; pageNumber++) {
                String fullLink = "%s%s%d%s".formatted(SOURCE_LINK, PREFIX, pageNumber, SUFFIX);
                var connection = Jsoup.connect(fullLink);
                var document = connection.get();
                var rows = document.select(".vacancy-card__inner");
                rows.forEach(row -> {
                    var titleElement = row.select(".vacancy-card__title").first();
                    var linkElement = titleElement.child(0);
                    String vacancyName = titleElement.text();
                    String link = String.format("%s%s", SOURCE_LINK,
                            linkElement.attr("href"));
                    var dateElement = row.select(".vacancy-card__date").first();
                    var dateTimeElement = dateElement.child(0);
                    String dateTime = dateTimeElement.attr("datetime");
                    Long time = dateTimeParser.parse(dateTime)
                            .atZone(ZoneId.systemDefault()).toInstant().getEpochSecond();
                    var description = retrieveDescription(link);
                    var post = new Post();
                    post.setTitle(vacancyName);
                    post.setLink(link);
                    post.setTime(time);
                    post.setDescription(description);
                    result.add(post);
                });
            }
        } catch (IOException e) {
            LOG.error("When load page", e);
        }
        return result;
    }

    private String retrieveDescription(String link) {
        StringJoiner joiner = new StringJoiner(System.lineSeparator());
        try {
            var connection = Jsoup.connect(link);
            var document = connection.get();
            var description = document.select(".vacancy-description__text").first();
            var descriptionElements = description.children();
            descriptionElements.forEach(element -> {
                        if (element.stream().anyMatch(e -> e.hasText())) {
                            var list = element.getAllElements().eachText();
                            list.stream().forEach(joiner::add);
                        }
                    });
        } catch (IOException e) {
            LOG.error("When load description", e);
        }
        return joiner.toString();
    }
}
