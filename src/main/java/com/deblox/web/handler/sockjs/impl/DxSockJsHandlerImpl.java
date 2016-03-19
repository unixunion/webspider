package com.deblox.web.handler.sockjs.impl;

import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.handler.sockjs.impl.SockJSHandlerImpl;

/**
 * Created by keghol on 23/02/16.
 */
public class DxSockJsHandlerImpl extends SockJSHandlerImpl {

  private static final Logger logger = LoggerFactory.getLogger(DxSockJsHandlerImpl.class);

  public DxSockJsHandlerImpl(Vertx vertx, SockJSHandlerOptions options) {
    super(vertx, options);
  }

  /**
   * Check user principal is allowed to websocket before passing to super
   *
   * @param context
   */
  @Override
  public void handle(RoutingContext context) {
    logger.info("Handling Message user:" + context.session().get("user"));
    logger.info("User: " + context.user());

    super.handle(context);

//    if (context.user() == null) {
//      logger.warn("No user session");
//      context.fail(400);
//    }
//
//    context.user().isAuthorised("websocket", ctx -> {
//      logger.info("Checking if user is authorized for websocket");
//      if (ctx.result()) {
//        logger.info(context);
//        super.handle(context);
//      } else {
//        logger.warn("User does not have websocket permissions");
//        context.fail(400);
//      }
//    });
  }


}
