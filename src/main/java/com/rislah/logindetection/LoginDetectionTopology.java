package com.rislah.logindetection;

import com.rislah.logindetection.model.UserLoginAttempt;
import com.rislah.logindetection.model.serialization.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoginDetectionTopology {
  private static final Logger log = LoggerFactory.getLogger(LoginDetectionTopology.class);
  private final StreamsBuilder builder;

  public LoginDetectionTopology() {
    this.builder = new StreamsBuilder();
  }

  public KStream<Integer, UserLoginAttempt> unknownIpsStream(
      KTable<String, UserLoginAttempt> knownIpsTable,
      KStream<byte[], UserLoginAttempt> userLoginAttemptStream) {
    ValueJoiner<UserLoginAttempt, UserLoginAttempt, UserLoginKnown> unknownValueJoiner =
        (left, right) -> {
          UserLoginKnown userLoginAttemptEnriched = new UserLoginKnown(left, true);
          if (right == null) {
            userLoginAttemptEnriched.setKnown(false);
          }
          return userLoginAttemptEnriched;
        };

    Joined<String, UserLoginAttempt, UserLoginAttempt> unknownJoinParams =
        Joined.<String, UserLoginAttempt, UserLoginAttempt>as("unknowns_join_params")
            .withKeySerde(Serdes.String())
            .withValueSerde(JsonSerdes.UserLoginAttempt())
            .withOtherValueSerde(JsonSerdes.UserLoginAttempt());

    return userLoginAttemptStream
        .filterNot(
            (k, v) -> v == null || v.getResult() == UserLoginAttempt.Result.FAILURE,
            Named.as("filter_out_null_or_failure"))
        .selectKey(
            (k, userLoginAttempt) ->
                String.format("%d:%s", userLoginAttempt.getUserId(), userLoginAttempt.getIp()),
            Named.as("select_userid_ip_key"))
        .leftJoin(knownIpsTable, unknownValueJoiner, unknownJoinParams)
        .filterNot((k, v) -> v.getKnown(), Named.as("filter_out_known"))
        .map((k, v) -> KeyValue.pair(v.attempt.getUserId(), v.attempt), Named.as("map_back"));
  }

  public Topology build() {
    KStream<byte[], UserLoginAttempt> loginAttemptStream =
        builder.stream(
                Topics.USER_LOGIN_ATTEMPT.name(),
                Consumed.<byte[], UserLoginAttempt>as(
                        String.format("%s_topic", Topics.USER_LOGIN_ATTEMPT.name()))
                    .withKeySerde(Topics.USER_LOGIN_ATTEMPT.keySerde())
                    .withValueSerde(Topics.USER_LOGIN_ATTEMPT.valueSerde()))
            .peek((k, v) -> log.info("{}: [{}] {}", Topics.USER_LOGIN_ATTEMPT.name(), k, v));

    KStream<Integer, UserLoginAttempt> knownIpsStream =
        builder.stream(
                Topics.USER_KNOWN_IPS.name(),
                Consumed.<Integer, UserLoginAttempt>as(
                        String.format("%s_topic", Topics.USER_KNOWN_IPS.name()))
                    .withKeySerde(Topics.USER_KNOWN_IPS.keySerde())
                    .withValueSerde(Topics.USER_KNOWN_IPS.valueSerde()))
            .peek((k, v) -> log.info("{}: [{}] {}", Topics.USER_KNOWN_IPS.name(), k, v));

    KTable<String, UserLoginAttempt> knownIpsTable =
        knownIpsStream
            .selectKey((k, v) -> String.format("%d:%s", v.getUserId(), v.getIp()))
            .toTable(
                Named.as("known_ips_stream_table"),
                Materialized.<String, UserLoginAttempt, KeyValueStore<Bytes, byte[]>>as(
                        "known_ips_stream_table_store")
                    .withKeySerde(Serdes.String())
                    .withValueSerde(JsonSerdes.UserLoginAttempt()));

    KStream<Integer, UserLoginAttempt> unknownIpsStream =
        unknownIpsStream(knownIpsTable, loginAttemptStream);

    unknownIpsStream.to(
        Topics.USER_UNKNOWN_IP.name(),
        Produced.<Integer, UserLoginAttempt>as(
                String.format("%s_output_topic", Topics.USER_UNKNOWN_IP.name()))
            .withKeySerde(Topics.USER_UNKNOWN_IP.keySerde())
            .withValueSerde(Topics.USER_UNKNOWN_IP.valueSerde()));

    // first successful login is counted as a known ip
    unknownIpsStream.to(
        Topics.USER_KNOWN_IPS.name(),
        Produced.<Integer, UserLoginAttempt>as(
                String.format("%s_output_topic", Topics.USER_KNOWN_IPS.name()))
            .withKeySerde(Topics.USER_KNOWN_IPS.keySerde())
            .withValueSerde(Topics.USER_KNOWN_IPS.valueSerde()));

    return builder.build();
  }

  public static class UserLoginKnown {
    private final UserLoginAttempt attempt;
    private Boolean isKnown;

    public UserLoginKnown(UserLoginAttempt attempt, Boolean isKnown) {
      this.attempt = attempt;
      this.isKnown = isKnown;
    }

    public Boolean getKnown() {
      return isKnown;
    }

    public void setKnown(Boolean known) {
      isKnown = known;
    }
  }
}
