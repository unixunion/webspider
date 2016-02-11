
package com.deblox.servers;

import com.deblox.templating.DxTemplateEngine;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Cookie;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.TemplateHandler;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;


public class DxHttpServer extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(DxHttpServer.class);
  EventBus eb;

  @Override
  public void start(Future<Void> startFuture) {
    logger.info("Starting Up with Config: " + config().toString());

    // Set the default configs
    String webrootPath = config().getString("webroot", "webroot");
    String templatePath = config().getString("templates", "templates");

    Router router = Router.router(vertx);

    // Allow events for the designated addresses in/out of the event bus bridge
    BridgeOptions opts = new BridgeOptions()
            .addInboundPermitted(new PermittedOptions())
            .addOutboundPermitted(new PermittedOptions());

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);

    // This cookie handler will be called for all routes
    router.route().handler(CookieHandler.create());

    router.route("/eventbus/*").handler(ebHandler).failureHandler(frc -> {
      frc.response().setStatusCode(500);
      frc.response().end("Eventbus Error, " + frc.failure().getLocalizedMessage());
    });

    // redirect / request to the dynamic template start page
    router.get("/").handler(res -> {
      res.response().setStatusCode(302);
      res.response().headers().set("Location", "/index.templ");
      res.response().end();
    });

    // Cookies
    router.route().handler(ctx -> {
      Cookie someCookie = ctx.getCookie("visits");

      long visits = 0;
      if (someCookie != null) {
        String cookieValue = someCookie.getValue();
        try {
          visits = Long.parseLong(cookieValue);
        } catch (NumberFormatException e) {
          visits = 0l;
        }
      }

      // increment the tracking
      visits++;

      // Add a cookie - this will get written back in the response automatically
      ctx.addCookie(Cookie.cookie("visits", "" + visits));

      ctx.next();
    });

    // Serve the static
    router.route("/static/*").handler(StaticHandler.create(webrootPath)).failureHandler(frc -> {
      frc.response().setStatusCode(404);
      frc.response().end("Static Content Error, " + frc.failure().getLocalizedMessage());
    });

    // dynamic router for "template" driven content
    router.route().handler(TemplateHandler.create(DxTemplateEngine.create(templatePath))).failureHandler(frc -> {
      frc.response().setStatusCode(500);
      frc.response().end("Template Error, " + frc.failure().getLocalizedMessage());
    });


    // the server itself
    vertx.createHttpServer().requestHandler(router::accept).listen(config().getInteger("port", 8080));

    eb = vertx.eventBus();

    // send back deployment complete
    startFuture.complete();
  }

  @Override
  public void stop(Future<Void> stopFuture) {
    eb.publish("broadcast", "DxHttpServer shutting down");

    vertx.setTimer(1000, tid -> {
      logger.info("shutdown");
      stopFuture.complete();
    });

  }

}