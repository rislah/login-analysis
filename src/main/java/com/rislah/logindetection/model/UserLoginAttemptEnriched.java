package com.rislah.logindetection.model;

public class UserLoginAttemptEnriched {
  private final UserLoginAttempt attempt;
  private Boolean isKnown;

  public UserLoginAttemptEnriched(UserLoginAttempt attempt, Boolean isKnown) {
    this.attempt = attempt;
    this.isKnown = isKnown;
  }

  public UserLoginAttempt getAttempt() {
    return attempt;
  }

  public Boolean getKnown() {
    return isKnown;
  }

  public void setKnown(Boolean known) {
    isKnown = known;
  }

  @Override
  public String toString() {
    return "UserLoginAttemptEnriched{" + "attempt=" + attempt + ", isKnown=" + isKnown + '}';
  }
}
