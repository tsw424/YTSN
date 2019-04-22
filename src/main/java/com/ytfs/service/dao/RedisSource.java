package com.ytfs.service.dao;

import com.mongodb.MongoException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import redis.clients.jedis.BasicCommands;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.exceptions.JedisException;

public class RedisSource {

    private static RedisSource source = null;

    private static void newInstance() {
        if (source != null) {
            return;
        }
        try {
            synchronized (RedisSource.class) {
                if (source == null) {
                    source = new RedisSource();
                }
            }
        } catch (Exception r) {
            try {
                Thread.sleep(15000);
            } catch (InterruptedException ex) {
            }
            throw new JedisException(r.getMessage());
        }
    }

    public static BasicCommands getJedis() {
        newInstance();
        return source.jedis;
    }

    public static void terminate() {
        synchronized (RedisSource.class) {
            if (source != null) {
                source.close();
            }
        }
    }
    private static final Logger LOG = Logger.getLogger(RedisSource.class);
    private BasicCommands jedis = null;

    private RedisSource() throws JedisException {
        String path = System.getProperty("mongo.conf", "conf/mongo.properties");
        try (InputStream inStream = new FileInputStream(path)) {
            Properties p = new Properties();
            p.load(inStream);
            init(p);
        } catch (Exception e) {
            close();
            throw e instanceof JedisException ? (JedisException) e : new JedisException(e.getMessage());
        }
    }

    private void close() {
        if (jedis != null) {
            if (jedis instanceof JedisCluster) {
                try {
                    ((JedisCluster) jedis).close();
                } catch (IOException ex) {
                }
            } else {
                ((Jedis) jedis).close();
            }
        }
    }

    private void init(Properties p) {
        String hostlist = p.getProperty("redislist");
        if (hostlist == null || hostlist.trim().isEmpty()) {
            throw new MongoException("MongoSource.properties文件中没有指定redislist");
        }
        String[] hosts = hostlist.trim().split(",");
        Set<HostAndPort> nodes = new HashSet<>();
        for (String host : hosts) {
            try {
                if (host != null) {
                    String[] addr = host.trim().split("\\:");
                    HostAndPort hostAndPort = new HostAndPort(addr[0], Integer.parseInt(addr[1]));
                    nodes.add(hostAndPort);
                    LOG.info("[" + hostAndPort.toString() + "]添加到服务器列表中...");
                }
            } catch (Exception r) {
            }
        }
        if (nodes.isEmpty()) {
            throw new MongoException("MongoSource.properties文件中没有指定redislist");
        }
        if (nodes.size() < 3) {
            HostAndPort hp = nodes.iterator().next();
            jedis = new Jedis(hp.getHost(), hp.getPort());
        } else {
            jedis = new JedisCluster(nodes);
        }
        LOG.info("连接服务器成功!");
    }
}
