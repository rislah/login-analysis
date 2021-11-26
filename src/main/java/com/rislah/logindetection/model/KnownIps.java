package com.rislah.logindetection.model;

import java.util.ArrayList;
import java.util.List;

public class KnownIps {
  private final List<String> ips = new ArrayList<>();

  public KnownIps add(String ip) {
    ips.add(ip);
    return this;
  }

  public KnownIps remove(String ip) {
    ips.remove(ip);
    return this;
  }

  public boolean contains(String ip) {
    return ips.contains(ip);
  }

  @Override
  public String toString() {
    return "KnownIps{" + "ips=" + ips + '}';
  }
}
