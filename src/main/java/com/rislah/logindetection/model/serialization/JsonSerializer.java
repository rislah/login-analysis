package com.rislah.logindetection.model.serialization;

import com.google.gson.Gson;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerializer<T> implements Serializer<T> {
  private final Gson gson = new Gson();

  @Override
  public byte[] serialize(String topic, T data) {
    if (data == null) {
      return null;
    }

    return gson.toJson(data).getBytes(StandardCharsets.UTF_8);
  }
}
