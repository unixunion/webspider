package com.deblox.web.handler.impl;

import com.deblox.web.handler.DxBlogHandler;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by keghol on 17/02/16.
 */
public class DxBlogHandlerImpl implements DxBlogHandler, Handler<RoutingContext> {

  private static final Logger logger = LoggerFactory.getLogger(DxBlogHandlerImpl.class);

  @Override
  public void handle(RoutingContext context) {
    logger.info("Handling Request");
    context.next();
  }
}
