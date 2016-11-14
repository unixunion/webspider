package com.deblox.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Created by keghol on 24/02/16.
 */
public class Pinger extends AbstractVerticle implements Handler<Message> {

  private static final Logger logger = LoggerFactory.getLogger(Pinger.class);
  public static EventBus eb;

  @Override
  public void start(final Future<Void> startFuture) {

    logger.info("Startup with Config: " + config().toString());

    eb = vertx.eventBus();

    eb.consumer("ping-address", message -> {
      logger.info("Replying to ping");
      message.reply("reply to ping");
    });


    startFuture.complete();

    logger.info("Startup Complete");

  }

  @Override
  public void handle(Message event) {
    logger.info("Handling message " + event.toString());
  }

}