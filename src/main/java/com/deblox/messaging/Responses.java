package com.deblox.messaging;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by keghol on 11/02/16.
 */
public class Responses {

  public static void sendOK(String className, Message<?> msg, JsonObject data) {
    JsonObject response = new JsonObject();
    response.put("status", "ok");
    response.put("class", className);
    response.put("data", data);
    sendMsg(className, msg, response);
  }

  public static void sendOK(String className, Message<?> msg) {
    JsonObject response = new JsonObject();
    response.put("status", "ok");
    response.put("class", className);
    sendMsg(className, msg, response);
  }

  public static void sendError(String className, Message<?> msg, String reason) {
    JsonObject response = new JsonObject();
    response.put("status", "error");
    response.put("data", reason);
    sendMsg(className, msg, response);
  }

  static void sendMsg(String className, Message<?> msg, JsonObject document) {
    document.put("class", className);
    msg.reply(document);
  }

}
