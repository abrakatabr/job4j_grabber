package ru.job4j.grabber.stores;

import org.apache.log4j.Logger;
import ru.job4j.grabber.model.Post;
import ru.job4j.grabber.service.SchedulerManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcStore implements Store {
    private static final Logger LOG = Logger.getLogger(JdbcStore.class);
    private final Connection connection;

    public JdbcStore(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void save(Post post) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO post(name, text, link, created)"
                            + "VALUES(?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS
            );
            statement.setString(1, post.getTitle());
            statement.setString(2, post.getDescription());
            statement.setString(3, post.getLink());
            if (post.getTime() == null) {
                post.setTime(System.currentTimeMillis());
            }
            statement.setTimestamp(4, new Timestamp(post.getTime()));
            statement.execute();
            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    post.setId(generatedKeys.getLong(1));
                }
            }
        } catch (SQLException e) {
            LOG.error("When save to db", e);
        }
    }

    @Override
    public List<Post> getAll() {
        List<Post> list = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM post"
            );
            {
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        list.add(new Post(
                                resultSet.getLong("id"),
                                resultSet.getString("name"),
                                resultSet.getString("link"),
                                resultSet.getString("text"),
                                resultSet.getTimestamp("created").getTime()
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("When get list of posts from db", e);
        }
        return list;
    }

    @Override
    public Optional<Post> findById(Long id) {
        Post post = new Post();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT FROM post WHERE id = ?"
            );
            {
                statement.setLong(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        post.setId(resultSet.getLong("id"));
                        post.setTitle(resultSet.getString("name"));
                        post.setLink(resultSet.getString("link"));
                        post.setDescription(resultSet.getString("text"));
                        post.setTime(resultSet.getTimestamp("created").getTime());
                    }
                }
            }
        } catch (SQLException e) {
            LOG.error("When get post by id from db", e);
        }
        return Optional.ofNullable(post);
    }
}
