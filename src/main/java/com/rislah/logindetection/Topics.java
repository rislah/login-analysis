package com.rislah.logindetection;

import com.rislah.logindetection.model.UserLoginAttempt;
import com.rislah.logindetection.model.serialization.JsonSerdes;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class Topics {
  public static Topic<Integer, UserLoginAttempt> USER_UNKNOWN_IP;
  public static Topic<Void, UserLoginAttempt> USER_LOGIN_ATTEMPT;
  public static Topic<Integer, UserLoginAttempt> USER_KNOWN_IPS;

  static {
    createTopics();
  }

  private static void createTopics() {
    USER_KNOWN_IPS =
        new Topic<>("internal.identity.known-ips", Serdes.Integer(), JsonSerdes.UserLoginAttempt());
    USER_LOGIN_ATTEMPT =
        new Topic<>(
            "tracking.identity.login-attempt", Serdes.Void(), JsonSerdes.UserLoginAttempt());
    USER_UNKNOWN_IP =
        new Topic<>(
            "tracking.identity.unknown-ip", Serdes.Integer(), JsonSerdes.UserLoginAttempt());
  }

  public record Topic<K, V>(String name, Serde<K> keySerde, Serde<V> valueSerde) {}
}
