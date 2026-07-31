package com.api.auth_service.api.db;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class DatabaseClient {

    private static final JdbcTemplate jdbcTemplate;

    static {
        try {
            Properties props = new Properties();
            try (InputStream input = DatabaseClient.class.getClassLoader()
                    .getResourceAsStream("application-test.properties")) {
                if (input != null) {
                    props.load(input);
                } else {
                    props.setProperty("spring.datasource.url", "jdbc:postgresql://localhost:5435/accrefull_db_service");
                    props.setProperty("spring.datasource.username", "accrefull_admin");
                    props.setProperty("spring.datasource.password", "accrefull123");
                    props.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
                }
            }

            String url = props.getProperty("spring.datasource.url");
            String username = props.getProperty("spring.datasource.username");
            String password = props.getProperty("spring.datasource.password");
            String driver = props.getProperty("spring.datasource.driver-class-name");

            DriverManagerDataSource dataSource = new DriverManagerDataSource();
            dataSource.setUrl(url);
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            dataSource.setDriverClassName(driver);

            jdbcTemplate = new JdbcTemplate(dataSource);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка подключения к БД", e);
        }
    }

    // ========== ПОЛЬЗОВАТЕЛИ ==========

    public static boolean userExists(Long userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public static boolean userExistsByLogin(String login) {
        String sql = "SELECT COUNT(*) FROM users WHERE login = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, login);
        return count != null && count > 0;
    }

    public static boolean userExistsByEmail(String email) {
        String sql = "SELECT COUNT(*) FROM users WHERE email = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, email);
        return count != null && count > 0;
    }

    public static Map<String, Object> getUser(Long userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForMap(sql, userId);
    }

    public static Map<String, Object> getUserByLogin(String login) {
        String sql = "SELECT * FROM users WHERE login = ?";
        return jdbcTemplate.queryForMap(sql, login);
    }

    public static Map<String, Object> getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        return jdbcTemplate.queryForMap(sql, email);
    }

    public static String getUserPasswordHash(Long userId) {
        String sql = "SELECT password FROM users WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, String.class, userId);
    }

    public static void deleteUser(Long userId) {
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public static void deleteUserByLogin(String login) {
        String sql = "DELETE FROM users WHERE login = ?";
        jdbcTemplate.update(sql, login);
    }

    public static void deleteUserByEmail(String email) {
        String sql = "DELETE FROM users WHERE email = ?";
        jdbcTemplate.update(sql, email);
    }

    // ========== КОМАНДЫ ==========

    public static boolean teamExists(Long teamId) {
        String sql = "SELECT COUNT(*) FROM teams WHERE team_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, teamId);
        return count != null && count > 0;
    }

    public static Map<String, Object> getTeam(Long teamId) {
        String sql = "SELECT * FROM teams WHERE team_id = ?";
        return jdbcTemplate.queryForMap(sql, teamId);
    }

    public static void deleteTeam(Long teamId) {
        String sql = "DELETE FROM teams WHERE team_id = ?";
        jdbcTemplate.update(sql, teamId);
    }

    // ========== ДОЛЖНОСТИ ==========

    public static boolean positionExists(Long positionId) {
        String sql = "SELECT COUNT(*) FROM positions WHERE position_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, positionId);
        return count != null && count > 0;
    }

    public static Map<String, Object> getPosition(Long positionId) {
        String sql = "SELECT * FROM positions WHERE position_id = ?";
        return jdbcTemplate.queryForMap(sql, positionId);
    }

    public static void deletePosition(Long positionId) {
        String sql = "DELETE FROM positions WHERE position_id = ?";
        jdbcTemplate.update(sql, positionId);
    }

    // ========== ОБЩИЕ МЕТОДЫ ==========

    public static List<Map<String, Object>> executeQuery(String sql) {
        return jdbcTemplate.queryForList(sql);
    }

    public static int executeUpdate(String sql, Object... args) {
        return jdbcTemplate.update(sql, args);
    }

    public static JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }
}