package com.rislah.logindetection;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class App {
  public static final Logger log = LoggerFactory.getLogger(App.class);

  public static void main(String[] args) {
    final Config config = ConfigFactory.load();
    final Config kafkaStreamsConfig = config.getConfig("kafka-streams");

    Properties kafkaProperties = new Properties();
    kafkaStreamsConfig
        .entrySet()
        .forEach(
            e -> kafkaProperties.setProperty(e.getKey(), kafkaStreamsConfig.getString(e.getKey())));

    final LoginDetectionTopology loginDetectionTopology = new LoginDetectionTopology();
    final Topology topology = loginDetectionTopology.build();

    final KafkaStreams streams = new KafkaStreams(topology, kafkaProperties);
    final CountDownLatch latch = new CountDownLatch(1);

    streams.setStateListener(
        (newState, oldState) -> {
          if (newState == KafkaStreams.State.RUNNING && oldState != KafkaStreams.State.RUNNING) {
            latch.countDown();
          }
        });
    streams.start();

    log.info(topology.describe().toString());

    try {
      if (!latch.await(60, TimeUnit.SECONDS)) {
        throw new RuntimeException("Streams never finished re-balancing on startup");
      }
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
  }
}
