package ru.job4j.grabber.service;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import ru.job4j.grabber.stores.Store;
import ru.job4j.grabber.utils.DateTimeParser;
import ru.job4j.grabber.utils.HabrCareerDateTimeParser;

public class SuperJobGrab implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        var store = (Store) context.getJobDetail().getJobDataMap().get("store");
        DateTimeParser dateTimeParser = new HabrCareerDateTimeParser();
        HabrCareerParse parser = new HabrCareerParse(dateTimeParser);
        parser.fetch().stream().forEach(store::save);
        for (var post : store.getAll()) {
            System.out.println(post.getTitle());
        }
    }
}
