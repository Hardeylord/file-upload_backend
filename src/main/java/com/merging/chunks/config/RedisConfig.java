package com.merging.chunks.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisClientConfig;
import redis.clients.jedis.UnifiedJedis;

@Configuration
public class RedisConfig {
    String host =System.getenv("REDIS_HOST");
    String port =System.getenv("REDIS_PORT");
    String password =System.getenv("REDIS_PASSWORD");

    @Bean
    public RedisConnectionFactory connectionFactory () {
        var config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(Integer.parseInt(port));
        config.setPassword(RedisPassword.of(password));

        JedisClientConfiguration clientConfig = JedisClientConfiguration.builder()
                .build();

        return new JedisConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        var template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());
        template.setHashValueSerializer(RedisSerializer.json());
        template.afterPropertiesSet();
        return template;
    }
}

//

//import redis.clients.jedis.UnifiedJedis;
//import redis.clients.jedis.DefaultJedisClientConfig;
//import redis.clients.jedis.HostAndPort;
//import redis.clients.jedis.JedisClientConfig;
//
//public class ConnectBasicExample {
//    public void run() {
//        JedisClientConfig config = DefaultJedisClientConfig.builder()
//                .user("default")
//                .password("xAHUbLNeQRBfRoFBisYmlpA3r6jCsSkT")
//                .build();
//
//        UnifiedJedis jedis = new UnifiedJedis(
//                new HostAndPort("redis-13135.c61.us-east-1-3.ec2.cloud.redislabs.com", 13135),
//                config
//        );
//
//        String res1 = jedis.set("foo", "bar");
//        System.out.println(res1); // >>> OK
//
//        String res2 = jedis.get("foo");
//        System.out.println(res2); // >>> bar
//
//        jedis.close();
//    }
//}
