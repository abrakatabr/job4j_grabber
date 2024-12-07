package ru.job4j;

import org.apache.log4j.Logger;
import ru.job4j.grabber.service.Config;
import ru.job4j.grabber.service.SchedulerManager;
import ru.job4j.grabber.service.Web;
import ru.job4j.grabber.service.SuperJobGrab;
import ru.job4j.grabber.stores.JdbcStore;

import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        var config = new Config();
        config.load("src/main/resources/application.properties");
        try {
            var connection = DriverManager.getConnection(
                    config.get("db.url"), config.get("db.username"), config.get("db.password")
            );
            var store = new JdbcStore(connection);
            var scheduler = new SchedulerManager();
            scheduler.init();
            scheduler.load(
                    Integer.parseInt(config.get("rabbit.interval")),
                    SuperJobGrab.class,
                    store);
            new Web(store).start(Integer.parseInt(config.get("server.port")));
        } catch (SQLException e) {
            LOG.error("When create a connection", e);
            }
        }
    }
