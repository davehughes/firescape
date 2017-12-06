package org.firescape.server.io;

import org.firescape.server.util.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Ent Loads Player details up.
 */
public class PlayerLoader {

  // TODO: convert this to an enum
  public static final int LOGIN_STATE_BAD_PASSWORD = 0;
  public static final int LOGIN_STATE_CORRECT_PASSWORD = 1;
  public static final int LOGIN_STATE_ALREADY_LOGGED_IN = 2;
  public static final int LOGIN_STATE_BANNED = 6;
  public static final int LOGIN_STATE_BAD_USER_OR_PASSWORD = 22;

  public enum LoginState {
    BAD_PASSWORD,
    CORRECT_PASSWORD,
    ALREADY_LOGGED_IN,
    BANNED,
  }

  private JedisPool _redisPool;

  public PlayerLoader() {
    this(new JedisPool(new JedisPoolConfig(), "localhost"));
  }

  public PlayerLoader(JedisPool redisPool) {
    _redisPool = redisPool; 
  }

  public LoginState getLogin(String username, String password) {
    try {
        Properties props = loadPlayerProperties(username);
        if (props == null) {
            props = createPlayer(username, password);
        }

        if (Integer.valueOf(props.getProperty("rank")) == LOGIN_STATE_BANNED) {
          return LOGIN_STATE_BANNED; // Banned.
        }

        if (props.getProperty("pass").equalsIgnoreCase(password)) {
          if (props.getProperty("loggedin").equalsIgnoreCase("true")) {
            return LOGIN_STATE_ALREADY_LOGGED_IN; // Already logged in.
          }
          return LOGIN_STATE_CORRECT_PASSWORD; // Correct, Log in.
        }
    } catch (Exception e) {
      e.printStackTrace();
      Logger.print(e.toString(), 1);
    }

      // Bad password or other failure to log in
      return LOGIN_STATE_BAD_PASSWORD;
  }

  private String redisKeyForUser(String username) {
      return "players_" + username.replaceAll(" ", "_").toLowerCase();
  }

  private Properties loadPlayerProperties(String username) throws IOException {
    Jedis jedis = _redisPool.getResource();
    String key = redisKeyForUser(username);
    if (!jedis.exists(key)) {
        return null;
    }

    byte[] keyBytes = jedis.get(key).getBytes(StandardCharsets.UTF_8);
    InputStream ios = new ByteArrayInputStream(keyBytes);
    Logger.print("Loaded " + key + " from redis.", 3);

    Properties props = new Properties();
    props.load(ios);
    ios.close();

    return props;
  }

  private Properties createPlayer(String username, String password) throws IOException {
    Jedis jedis = _redisPool.getResource();
    String key = redisKeyForUser(username);
    Properties props = new Properties();
    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    props.load(new FileInputStream(new File("players/Template")));
    props.setProperty("pass", password);
    props.store(bos, "Redis backed character data");
    jedis.set(key, bos.toString());
    Logger.print("Saved " + key + " data to redis.", 3);
    // Server.writeValue(user, "pass", pass);
    Logger.print("Account Created: " + username, 3);
    return props;
  }
}
