package com.deblox.servers;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;


import static com.deblox.messaging.Responses.sendError;
import static com.deblox.messaging.Responses.sendOK;

/**
 * Eventbus auth server,
 *
 * accepts a JsonObject with keys username, password, should validate this and return the principal.
 *
 * Created by keghol on 12/02/16.
 */
public class AuthVerticle extends AbstractVerticle {
  private static final Logger logger = LoggerFactory.getLogger(AuthVerticle.class);

  public void start(Future<Void> startFuture) throws Exception {

    logger.info("Startup with Config: " + config().toString());

    vertx.eventBus().consumer("auth-address", message -> {

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
