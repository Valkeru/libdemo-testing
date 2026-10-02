package ru.valkeru.libdemo.test.util;

import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    private final RedisConnectionFactory connectionFactory;

    public RedisUtil(RedisConnectionFactory connectionFactory) {
        this.connectionFactory = connectionFactory;
    }

    public void clearCaches() {
        connectionFactory.getConnection().serverCommands().flushAll();
    }
}
