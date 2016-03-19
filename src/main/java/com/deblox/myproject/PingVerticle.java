package com.deblox.myproject;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.Future;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.UUID;


public class PingVerticle extends AbstractVerticle implements Handler<Message> {

  private static final Logger logger = LoggerFactory.getLogger(PingVerticle.class);
  public static EventBus eb;
  public static Vertx vx;

  public void start(Future<Void> startFuture) throws Exception {

    logger.info("Startup with Config: " + config().toString());

    vx = Vertx.vertx();
    eb = vertx.eventBus();

    eb.consumer("openin", message -> {
      message.reply("reply to openin " + message.body().toString());
    });

    vertx.setPeriodic(1000, res-> {
      eb.publish("openout", "openout");
    });

    // wait 1 second before completing startup
    vertx.setTimer(1000, tid -> {
      logger.info("startup complete");
      startFuture.complete();
    });

  }

  @Override
  public void handle(Message event) {
    logger.info("Handling message " + event.toString());
  }

}
