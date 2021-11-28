package com.rislah.logindetection.model;

import com.google.gson.annotations.SerializedName;

public class UserLoginAttempt {
  @SerializedName("user_id")
  private Integer userId;

  @SerializedName("ip")
  private String ip;

  @SerializedName("result")
  private Result result;

  public UserLoginAttempt() {}

  public UserLoginAttempt(Integer userId, String ip, Result result) {
    this.userId = userId;
    this.ip = ip;
    this.result = result;
  }

  public Integer getUserId() {
    return userId;
  }

  public String getIp() {
    return ip;
  }

  public Result getResult() {
    return result;
  }

  @Override
  public String toString() {
    return "UserLoginAttempt{"
        + "userId="
        + userId
        + ", ip='"
        + ip
        + '\''
        + ", result="
        + result
        + '}';
  }

  public enum Result {
    FAILURE,
    SUCCESS
  }
}
