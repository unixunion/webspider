package com.deblox.myproject;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Core is just a holder for the vertx and eventbus context for parts of the application that need access to it.
 *
 * Created by keghol on 17/02/16.
 */
public class Core extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(PingVerticle.class);
  public static EventBus eb;
  public static Vertx vx;

  public void start(Future<Void> startFuture) throws Exception {

    logger.info("Startup with Config: " + config().toString());

    vx = Vertx.vertx();
    eb = vertx.eventBus();

  }

}
