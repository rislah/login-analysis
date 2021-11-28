package com.rislah.logindetection;

import com.rislah.logindetection.model.UserLoginAttempt;
import org.apache.kafka.streams.*;
import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class LoginDetectionTopologyTest {
  public TopologyTestDriver setupTestDriver(Topology topology) {
    Properties kafkaProperties = new Properties();
    kafkaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "test");
    kafkaProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");
    return new TopologyTestDriver(topology, kafkaProperties);
  }

  @Test
  public void testUserLoginAttemptSuccess() {
    LoginDetectionTopology loginDetectionTopology = new LoginDetectionTopology();

    try (TopologyTestDriver topologyTestDriver = setupTestDriver(loginDetectionTopology.build())) {
      TestInputTopic<byte[], UserLoginAttempt> userLoginAttemptTopic =
          topologyTestDriver.createInputTopic(
              Topics.USER_LOGIN_ATTEMPT.name(),
              Topics.USER_LOGIN_ATTEMPT.keySerde().serializer(),
              Topics.USER_LOGIN_ATTEMPT.valueSerde().serializer());

      TestOutputTopic<Integer, UserLoginAttempt> unknownTopic =
          topologyTestDriver.createOutputTopic(
              Topics.USER_UNKNOWN_IP.name(),
              Topics.USER_UNKNOWN_IP.keySerde().deserializer(),
              Topics.USER_UNKNOWN_IP.valueSerde().deserializer());

      TestOutputTopic<Integer, UserLoginAttempt> knownIpsTopic =
          topologyTestDriver.createOutputTopic(
              Topics.USER_KNOWN_IPS.name(),
              Topics.USER_KNOWN_IPS.keySerde().deserializer(),
              Topics.USER_KNOWN_IPS.valueSerde().deserializer());

      UserLoginAttempt loginAttempt =
          new UserLoginAttempt(0, "90.191.99.39", UserLoginAttempt.Result.SUCCESS);

      userLoginAttemptTopic.pipeInput(loginAttempt);
      assertThat(unknownTopic.getQueueSize()).isEqualTo(1);

      KeyValue<Integer, UserLoginAttempt> unknownVal = unknownTopic.readKeyValue();
      assertThat(unknownVal.key).isEqualTo(loginAttempt.getUserId());
      assertThat(unknownVal.value).usingRecursiveComparison().isEqualTo(loginAttempt);

      assertThat(knownIpsTopic.getQueueSize()).isEqualTo(1);
      KeyValue<Integer, UserLoginAttempt> knownVal = knownIpsTopic.readKeyValue();
      assertThat(knownVal.key).isEqualTo(loginAttempt.getUserId());
      assertThat(knownVal.value).usingRecursiveComparison().isEqualTo(loginAttempt);

      userLoginAttemptTopic.pipeInput(loginAttempt);
      assertThat(unknownTopic.getQueueSize()).isEqualTo(0);
      assertThat(knownIpsTopic.getQueueSize()).isEqualTo(0);
    }
  }

  @Test
  public void testUserLoginAttemptFailure() {
    LoginDetectionTopology loginDetectionTopology = new LoginDetectionTopology();

    try (TopologyTestDriver topologyTestDriver = setupTestDriver(loginDetectionTopology.build())) {
      TestInputTopic<byte[], UserLoginAttempt> userLoginAttemptTopic =
          topologyTestDriver.createInputTopic(
              Topics.USER_LOGIN_ATTEMPT.name(),
              Topics.USER_LOGIN_ATTEMPT.keySerde().serializer(),
              Topics.USER_LOGIN_ATTEMPT.valueSerde().serializer());

      TestOutputTopic<Integer, UserLoginAttempt> unknownTopic =
          topologyTestDriver.createOutputTopic(
              Topics.USER_UNKNOWN_IP.name(),
              Topics.USER_UNKNOWN_IP.keySerde().deserializer(),
              Topics.USER_UNKNOWN_IP.valueSerde().deserializer());

      TestOutputTopic<Integer, UserLoginAttempt> knownIpsTopic =
          topologyTestDriver.createOutputTopic(
              Topics.USER_KNOWN_IPS.name(),
              Topics.USER_KNOWN_IPS.keySerde().deserializer(),
              Topics.USER_KNOWN_IPS.valueSerde().deserializer());

      UserLoginAttempt loginAttempt =
          new UserLoginAttempt(0, "90.191.99.21", UserLoginAttempt.Result.FAILURE);

      userLoginAttemptTopic.pipeInput(loginAttempt);
      assertThat(unknownTopic.getQueueSize()).isEqualTo(0);
      assertThat(knownIpsTopic.getQueueSize()).isEqualTo(0);
    }
  }
}
