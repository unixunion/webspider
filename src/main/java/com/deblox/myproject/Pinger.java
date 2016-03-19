package com.deblox.myproject;

import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;

/**
 * Created by keghol on 24/02/16.
 */
public class Pinger implements Handler<SockJSSocket> {

  private static final Logger logger = LoggerFactory.getLogger(Pinger.class);

  @Override
  public void handle(SockJSSocket event) {
    logger.info("Handling " + event.toString());
  }
}
