
package com.deblox.servers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;


public class DxHttpServer extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(DxHttpServer.class);
  EventBus eb;

  @Override
  public void start(Future<Void> startFuture) {
    logger.info("Starting Up");
    Router router = Router.router(vertx);

    // Allow events for the designated addresses in/out of the event bus bridge
    BridgeOptions opts = new BridgeOptions()
            .addInboundPermitted(new PermittedOptions())
            .addOutboundPermitted(new PermittedOptions());

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);
    router.route("/eventbus/*").handler(ebHandler);

    // Serve the static pages
    router.route().handler(StaticHandler.create());

    // the server itself
    vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("port", 8080));

    eb = vertx.eventBus();

    // send back deployment complete
    startFuture.complete();
  }

  @Override
  public void stop(Future<Void> stopFuture) {
    eb.publish("broadcast", "server going down");

    vertx.setTimer(1000, tid -> {
      logger.info("shutdown");
      stopFuture.complete();
    });

  }

}