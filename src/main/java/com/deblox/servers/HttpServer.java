
package com.deblox.servers;

import com.deblox.web.handler.DxAuthProvider;
import com.deblox.web.handler.DxBlogHandler;
import com.deblox.web.handler.sockjs.DxSockJsHandler;
import com.deblox.web.templ.DxTemplateEngine;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import io.vertx.ext.web.sstore.LocalSessionStore;


public class HttpServer extends AbstractVerticle {

  private static final Logger logger = LoggerFactory.getLogger(HttpServer.class);
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
    router.exceptionHandler(throwable ->{
      logger.error("Error");
    });

    AuthProvider dxAuthProvider = DxAuthProvider.create();

    FormLoginHandler formLoginHandler = FormLoginHandler.create(dxAuthProvider).setDirectLoggedInOKURL("/");

    // JWT Provider
//    JsonObject config = new JsonObject().put("keyStore", new JsonObject()
//            .put("path", "keystore.jceks")
//            .put("type", "jceks")
//            .put("password", "secret"));
//    AuthProvider jwtProvider = JWTAuth.create(vertx, config);

    // TemplateEngine
    DxTemplateEngine dxTemplateEngine = DxTemplateEngine.create(templatePath);
    // TemplateEngine for the insecure templates e.g. login
    DxTemplateEngine loginDxTemplateEngine = DxTemplateEngine.create(insecureTemplatePath);

    // Allow Eventbus events for the designated addresses in/out of the event bus bridge
    // this is for "authorized" users with websocket authority.
    BridgeOptions authorizedBridgeOpts = new BridgeOptions()
            .addInboundPermitted(new PermittedOptions().setRequiredAuthority("websocket"))
            .addOutboundPermitted(new PermittedOptions().setRequiredAuthority("websocket"));

    // The DxSocketJsHandler
    SockJSHandler ebHandler = DxSockJsHandler.create(vertx, new SockJSHandlerOptions()).bridge(authorizedBridgeOpts);


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


    // token handler
    router.route().handler(ctx -> {
      try {
        logger.info("Checking if user already has a token");
        String tokenId = ctx.getCookie("token").getValue();
        logger.info("Token ID: " + tokenId);
      } catch (NullPointerException e) {
        logger.info("User does not have a token");
      }
    });



    // add variable data to the context for all following routers, also attempt to migrate session from the cluster
    // if its not on this local node.
    router.route().handler(ctx -> {
      try {

        String sessionId  = ctx.getCookie("vertx-web.session").getValue();

        logger.info("Session ID: " + sessionId);


        // set some variables in the web context
        ctx.put("role", ctx.user().principal().getString("role"));
        ctx.session().put("token", ctx.user().principal().getString("token"));
        ctx.session().put("user", ctx.user().principal().getString("username"));
        ctx.response().putHeader("Set-Cookie", "token=" + ctx.user().principal().getString("token") + "; path=/");

        getVertx().sharedData().getClusterWideMap("sessions", rtx -> {
          if (rtx.succeeded()) {

            rtx.result().putIfAbsent(sessionId, ctx.user().principal(), res -> {
              if (res.succeeded()) {
                logger.info("Added to map " + res.result());
              } else {
                logger.error("Unable to add to map");
              }
            });
          } else {
            logger.error("Clusterwide maps not available");
          }
        });
      } catch (IllegalStateException e) {
        logger.error("Cluster not enabled, session migrations not possible");

      } catch (NullPointerException e) {
        e.printStackTrace();
        logger.error("User has no session data, checking cluster");

        try {

          getVertx().sharedData().<String,String>getClusterWideMap("sessions", crtx -> {
            String sessionId  = ctx.getCookie("vertx-web.session").getValue();

            if (crtx.succeeded()) {
              AsyncMap<String, String> map = crtx.result();
              map.get(sessionId, resGet -> {
                if (resGet.succeeded()) {
                  Object userPrincipal = resGet.result();
                  logger.info(userPrincipal.toString());
                }
              });

            } else {
              logger.error("Unable to open the sessions map on the cluster");
            }

          });
        } catch (IllegalStateException ex) {
          logger.error("clustering not enabled, sessions cannot be found in the cluster");
        }

      }
      ctx.next();
    });


    router.route("/eventbus/*").handler(ebHandler);

    // This must be below eventbus bridge
    router.route().handler(BodyHandler.create());

    // Serve the static
    router.route("/static/*").handler(StaticHandler.create(webrootPath));

    // Handles the actual login POST
    router.route("/loginhandler").handler(formLoginHandler).failureHandler(ctx -> {
      ctx.response().setStatusCode(400);
      ctx.response().end("Unauthorized");
    });

    // Handles the logout GET
    router.route(HttpMethod.GET, "/logout").handler(ctx -> {
      ctx.session().destroy();
      ctx.response().setStatusCode(302);
      ctx.response().headers().set("Location", "/");
      ctx.response().end();
    });

    // "insecure" templates like login.
    router.route("/insecure/*").handler(TemplateHandler.create(loginDxTemplateEngine, insecureTemplatePath, "text/html"));


    // Any other requests require login
    router.route().handler(RedirectAuthHandler.create(dxAuthProvider, "/insecure/login.templ"));


    router.route(HttpMethod.POST, "/blog").handler(DxBlogHandler.create()).handler(res -> {
      logger.info("back from post new blog");
      res.next();
    });

    router.route(HttpMethod.GET, "/blog").handler(DxBlogHandler.create()).handler(res -> {
      logger.info("back from get blog");
      res.put("role", res.user().principal().getString("role"));
      res.session().put("user", res.user().principal().getString("username"));
      res.next();
    });

    // dynamic router for "template" driven content
    router.route().handler(TemplateHandler.create(dxTemplateEngine));

    // failure handler
//    router.route().failureHandler(ctx -> {
//      HttpServerResponse response = ctx.response();
//      response.end("Error Occurred");
//    });

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