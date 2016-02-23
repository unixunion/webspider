package com.deblox.servers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import static com.deblox.messaging.Responses.sendError;
import static com.deblox.messaging.Responses.sendOK;

/**
 * Created by keghol on 19/02/16.
 */
public class AdAuthVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(AdAuthVerticle.class);

  public void start(Future<Void> startFuture) throws Exception {

    logger.info("Startup with Config: " + config().toString());

    vertx.eventBus().consumer("ad-auth-address", message -> {

      logger.info("authenticate request " + message.body().toString());
      JsonObject authInfo = new JsonObject(message.body().toString());

      if ((authInfo.getString("username").equals("admin")) && (authInfo.getString("password").equals("admin"))) {
        JsonObject userPrincipal = authInfo;
        userPrincipal.put("role", "admin");
        sendOK(this.getClass().getSimpleName(), message, userPrincipal);

      } else {
        sendError(this.getClass().getSimpleName(), message, "Denied");
      }

    });

  }
}
