
package com.deblox.servers;

import com.deblox.auth.DxAuthProvider;
import com.deblox.auth.impl.DxAuthProviderImpl;
import com.deblox.templating.DxTemplateEngine;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;


public class DxHttpServer extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(DxHttpServer.class);
  EventBus eb;

  @Override
  public void start(Future<Void> startFuture) {
    logger.info("Starting Up with Config: " + config().toString());

    // Set the default configs
    String webrootPath = config().getString("webroot", "webroot");
    String templatePath = config().getString("templates", "templates");
    String insecureTemplatePath = templatePath + "/insecure";
    String jksFile = config().getString("jksFile", "/server-keystore.jks");
    String jksPassword = config().getString("jksPassword", "wibble");

    Router router = Router.router(vertx);

    // User Auth Providers
    DxAuthProvider dxAuthProvider = new DxAuthProviderImpl();

    // JWT Provider
//    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
//            .put("path", "keystore.jceks")
//            .put("type", "jceks")
//            .put("password", "secret"));
//    AuthProvider jwtProvider = JWTAuth.create(vertx, config);

    // TemplateEngine
    DxTemplateEngine dxTemplateEngine = DxTemplateEngine.create(templatePath);
    // TemplateEngine for the insecure templates e.g. login
    DxTemplateEngine loginTemplateEngine = DxTemplateEngine.create(insecureTemplatePath);

    // Allow Eventbus events for the designated addresses in/out of the event bus bridge
    // This is insecure, and requires locking down
    BridgeOptions opts = new BridgeOptions()
            .addInboundPermitted(new PermittedOptions())
            .addOutboundPermitted(new PermittedOptions());

    // Create the event bus bridge and add it to the router.
    SockJSHandler ebHandler = SockJSHandler.create(vertx).bridge(opts);

    // This cookie handler, session and usersession handler will be called for all routes
    router.route().handler(CookieHandler.create());
    router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx))
            .setCookieHttpOnlyFlag(true)
            .setCookieSecureFlag(true));
    router.route().handler(UserSessionHandler.create(dxAuthProvider));

    // redirect / request to the dynamic template start page
    router.get("/").handler(res -> {
      res.response().setStatusCode(302);
      res.response().headers().set("Location", "/index.templ");
      res.response().end();
    });

    // Cookies
//    router.route().handler(ctx -> {
//      Cookie someCookie = ctx.getCookie("visits");
//
//      long visits = 0;
//      if (someCookie != null) {
//        String cookieValue = someCookie.getValue();
//        try {
//          visits = Long.parseLong(cookieValue);
//        } catch (NumberFormatException e) {
//          visits = 0L;
//        }
//      }
//
//      // increment the tracking
//      visits++;
//      // Add a cookie - this will get written back in the response automatically
//      ctx.addCookie(Cookie.cookie("visits", "" + visits));
//      ctx.next();
//    });


    // Session handling
//    router.route().handler(ctx -> {
//      Session session = ctx.session();
//      Integer cnt = session.get("hitcount");
//      cnt = (cnt == null ? 0 : cnt) + 1;
//      session.put("hitcount", cnt);
//      ctx.next();
//    });

    router.route("/eventbus/*").handler(ebHandler).failureHandler(frc -> {
      frc.response().setStatusCode(500);
      frc.response().end("Eventbus Error, " + frc.failure().getLocalizedMessage());
    });

    // This must be below eventbus bridge
    router.route().handler(BodyHandler.create());

    // Serve the static
    router.route("/static/*").handler(StaticHandler.create(webrootPath)).failureHandler(frc -> {
      frc.response().setStatusCode(404);
      frc.response().end("Static Content Error, " + frc.failure().getLocalizedMessage());
    });

    // Handles the actual login POST
    router.route("/loginhandler").handler(FormLoginHandler.create(dxAuthProvider).setDirectLoggedInOKURL("/"));

    // "insecure" templates like login.
    router.route("/insecure/*").handler(TemplateHandler.create(loginTemplateEngine, insecureTemplatePath, "text/html"));

    // Any other requests require login
    router.route().handler(RedirectAuthHandler.create(dxAuthProvider, "/insecure/login.templ"));

    // dynamic router for "template" driven content
    router.route().handler(TemplateHandler.create(dxTemplateEngine));

    JksOptions jksOptions = new JksOptions()
            .setPath(jksFile)
            .setPassword(jksPassword);

    HttpServerOptions httpServerOptions = new HttpServerOptions()
            .setSsl(true)
            .setKeyStoreOptions(jksOptions);

    // the server itself
    vertx.createHttpServer(httpServerOptions).requestHandler(router::accept).listen(config().getInteger("port", 8080));

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