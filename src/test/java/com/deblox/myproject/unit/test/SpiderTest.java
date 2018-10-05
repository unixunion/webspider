package com.deblox.myproject.unit.test;

import com.deblox.spinnekop.Spider;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/*
 * Example of an asynchronous unit test written in JUnit style using vertx-unit
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */
@RunWith(VertxUnitRunner.class)
public class SpiderTest {

  Vertx vertx;
  EventBus eb;
  private static final Logger logger = LoggerFactory.getLogger(SpiderTest.class);

  @Before
  public void before(TestContext context) {
    logger.info("@Before");
    vertx = Vertx.vertx();
    eb = vertx.eventBus();

    Async async = context.async();
    vertx.deployVerticle(Spider.class.getName(), res -> {
      if (res.succeeded()) {
        async.complete();
      } else {
        res.cause().printStackTrace();
        context.fail();
      }
    });
  }

  @After
  public void after(TestContext context) {
    logger.info("@After");
    Async async = context.async();

    // the correct way after next release
    //vertx.close(context.assertAsyncSuccess());

    vertx.close( event -> {
      async.complete();
    });

  }

  @Test
  public void test(TestContext test) {
    Async async = test.async();
    eb.send("manager-address", new JsonObject().put("action", "spider").put("url", "https://stackoverflow.com"), reply -> {
      if (reply.succeeded()) {
          logger.info(reply.result().body().toString());
        async.complete();
      } else {
        test.fail();
      }
    });

  }
}