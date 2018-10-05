package com.deblox.myproject.unit.test;

import com.deblox.Util;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.deblox.Boot;

/*
 * Example of an asynchronous unit test written in JUnit style using vertx-unit
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class BootVerticleTest {

  Vertx vertx;
  EventBus eb;
  JsonObject config;
  private static final Logger logger = LoggerFactory.getLogger(BootVerticleTest.class);

  @Rule
  public RunTestOnContext rule = new RunTestOnContext();

  @Before
  public void before(TestContext context) {
    logger.info("@Before");
    vertx = rule.vertx();
    eb = vertx.eventBus();

    try {
      config = Util.loadConfig(BootVerticleTest.class, "/conf.json");
    } catch (Exception e) {
      logger.warn(e.getMessage());
    }

    Async async = context.async();
    vertx.deployVerticle(Boot.class.getName(), new DeploymentOptions(new JsonObject().put("config", config)),res -> {
      if (res.succeeded()) {
        async.complete();
      } else {
        context.fail();
      }
    });
  }


  @Test
  public void test(TestContext test) {
    Async async = test.async();
    logger.info("sending ping");
    eb.send("manager-address", new JsonObject().put("action", "ping"), reply -> {
      if (reply.succeeded()) {
        async.complete();
      } else {
        test.fail();
      }
    });

  }
}