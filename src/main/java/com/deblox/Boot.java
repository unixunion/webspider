package com.deblox;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.impl.LoggerFactory;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * Created by Kegan Holtzhausen on 29/05/14.
 *
 * This loads the config and then starts the main application services
 *
 */
public class Boot extends AbstractVerticle {
  JsonObject config;

  private static final Logger logger = LoggerFactory.getLogger(Boot.class);

  // Allow running direct from IDE, args "-conf conf.json"
  public static void main(String[] args) {

    ArgumentParser parser = ArgumentParsers.newArgumentParser("deBlox Boot")
            .defaultHelp(true)
            .description("development boot main");
    parser.addArgument("-conf")
            .help("config file");

    Namespace ns = null;
    try {
      ns = parser.parseArgs(args);
    } catch (ArgumentParserException e) {
      parser.handleError(e);
      System.exit(1);
    }

    // if conf is passed or not
    if (ns.get("conf") == null) {
      DebloxRunner.runJava("src/main/java", Boot.class, false);
    } else {
      DebloxRunner.runJava("src/main/java", Boot.class, false, ns.get("conf"));
    }

  }

  @Override
  public void start(final Future<Void> startedResult) {

    logger.info("\n" +
            "████████▄     ▄████████ ▀█████████▄   ▄█        ▄██████▄  ▀████    ▐████▀      ▀█████████▄   ▄██████▄   ▄██████▄      ███     \n" +
            "███   ▀███   ███    ███   ███    ███ ███       ███    ███   ███▌   ████▀         ███    ███ ███    ███ ███    ███ ▀█████████▄ \n" +
            "███    ███   ███    █▀    ███    ███ ███       ███    ███    ███  ▐███           ███    ███ ███    ███ ███    ███    ▀███▀▀██ \n" +
            "███    ███  ▄███▄▄▄      ▄███▄▄▄██▀  ███       ███    ███    ▀███▄███▀          ▄███▄▄▄██▀  ███    ███ ███    ███     ███   ▀ \n" +
            "███    ███ ▀▀███▀▀▀     ▀▀███▀▀▀██▄  ███       ███    ███    ████▀██▄          ▀▀███▀▀▀██▄  ███    ███ ███    ███     ███     \n" +
            "███    ███   ███    █▄    ███    ██▄ ███       ███    ███   ▐███  ▀███           ███    ██▄ ███    ███ ███    ███     ███     \n" +
            "███   ▄███   ███    ███   ███    ███ ███▌    ▄ ███    ███  ▄███     ███▄         ███    ███ ███    ███ ███    ███     ███     \n" +
            "████████▀    ██████████ ▄█████████▀  █████▄▄██  ▀██████▀  ████       ███▄      ▄█████████▀   ▀██████▀   ▀██████▀     ▄████▀   1.0\n" +
            "                                     ▀                                                                                        ");

    config = config();

    // warn a brother!
    if (config.equals(new JsonObject())) {
      logger.warn("you have no config here!");
    } else {
      logger.info("config: " + config);
    }

    // Start each class mentioned in services
    for (final Object service : config.getJsonArray("services", new JsonArray())) {

      logger.info("deploying service: " + service);

      // get the config for the named service
      JsonObject serviceConfigJson = config.getJsonObject(service.toString(), new JsonObject());
      logger.info("serviceConfigJson: " + serviceConfigJson);

      // See DeploymentOptions.fromJson for all the possible configurables
      DeploymentOptions serviceConfig = new DeploymentOptions(serviceConfigJson);

      vertx.deployVerticle(service.toString(), serviceConfig, res -> {

        if (res.succeeded()) {
          logger.info("successfully deployed service: " + service);
        } else {
          logger.error("failure while deploying service: " + service);
          res.cause().printStackTrace();
        }

      });

    }

    logger.info("startup complete");

    startedResult.complete();

  }
}


