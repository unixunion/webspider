package com.deblox.myproject;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.UUID;


public class PingVerticle extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(PingVerticle.class);
  public static EventBus eb;
  public static Vertx vx;
  String uuid;

  public void start(Future<Void> startFuture) throws Exception {

    logger.info("startup with config: " + config().toString());

    // create a uuid for identifying instances of this verticle
    uuid = UUID.randomUUID().toString();
    vx = Vertx.vertx();
    eb = vertx.eventBus();

    eb.consumer("ping-address", message -> {
      logger.info(uuid + ": replying");
      message.reply("pong!");
    });

    vertx.setPeriodic(1000, res-> {
      eb.publish("broadcast", "broadcast");
    });

    // wait 1 second before completing startup
    vertx.setTimer(1000, tid -> {
      logger.info(uuid + ": startup complete");
      startFuture.complete();
    });

  }
}
