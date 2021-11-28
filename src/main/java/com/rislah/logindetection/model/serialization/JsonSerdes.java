package com.rislah.logindetection.model.serialization;

import com.rislah.logindetection.model.KnownIps;
import com.rislah.logindetection.model.UserLoginAttempt;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {
  public static Serde<UserLoginAttempt> UserLoginAttempt() {
    JsonDeserializer<UserLoginAttempt> deserializer =
        new JsonDeserializer<>(UserLoginAttempt.class);
    JsonSerializer<UserLoginAttempt> serializer = new JsonSerializer<>();
    return Serdes.serdeFrom(serializer, deserializer);
  }

  public static Serde<KnownIps> KnownIps() {
    JsonDeserializer<KnownIps> deserializer = new JsonDeserializer<>(KnownIps.class);
    JsonSerializer<KnownIps> serializer = new JsonSerializer<>();
    return Serdes.serdeFrom(serializer, deserializer);
  }

//  public static Serde<UserLoginAttemptEnriched> UserLoginAttemptEnriched() {
//    JsonDeserializer<UserLoginAttemptEnriched> deserializer =
//        new JsonDeserializer<>(UserLoginAttemptEnriched.class);
//    JsonSerializer<UserLoginAttemptEnriched> serializer = new JsonSerializer<>();
//    return Serdes.serdeFrom(serializer, deserializer);
//  }
}
