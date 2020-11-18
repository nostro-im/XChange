package org.knowm.xchange.binancemargin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BinanceException extends RuntimeException {

  private final int code;

  public BinanceException(@JsonProperty("code") int code, @JsonProperty("msg") String msg) {
    super(msg);
    this.code = code;
  }

  public int getCode() {
    return code;
  }
}
