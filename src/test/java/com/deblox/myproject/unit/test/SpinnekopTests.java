package com.deblox.myproject.unit.test;

import com.deblox.spinnekop.DataCaptureService;
import com.deblox.spinnekop.SPage;
import com.deblox.spinnekop.Spinnekop;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class SpinnekopTests {

    private static final Logger logger = LoggerFactory.getLogger(SpinnekopTests.class);
    Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        Async async = context.async();
        vertx.deployVerticle(DataCaptureService.class.getName(), res -> {
            if (res.succeeded()) {
                async.complete();
            } else {
                context.fail();
            }
        });
    }

    @Test
    public void newlyCreatedSpage(TestContext test) {
        SPage sp = new SPage("http://foo.com");
        test.assertEquals(SPage.State.NEW, sp.getState());
        test.assertEquals("", sp.getBody());
        test.assertEquals("01F4420B64C9CBF03E293359105D55D04FF81D90", sp.getLinkHash());
        logger.info("done");
    }

    @Test
    public void getPage(TestContext test) {
        Async async = test.async();
        Spinnekop spinnekop = new Spinnekop(vertx);
        SPage page = new SPage("https://stackoverflow.com");



        spinnekop.getBody(page.getUrl(), bodyBuffer -> {
            if (bodyBuffer.succeeded()) {

                page.setBody(new String(bodyBuffer.result().getBytes()));
                //logger.info(page.getBody());

                spinnekop.getLinks(page.getUrl(), page.getBody(), resp->{
                    if (resp.succeeded()) {
                        page.setSublinks(resp.result());
                        page.getSublinks().forEach(link -> {
//                            logger.info("link: " + link);
                        });

                    } else {
                        logger.error("Error " + resp.cause());
                    }
                });

                vertx.eventBus().send("datacapture.service", page.serialize(), dsresp -> {
                    async.complete();
                });


            } else {
                test.fail();
            }
        });
    }

    @Test
    public void serializePage(TestContext test) {
        Spinnekop spinnekop = new Spinnekop(vertx);
        SPage page = new SPage("https://stackoverflow.com/");
        test.assertEquals("{\"url\":\"https://stackoverflow.com/\",\"sublinks\":[],\"body\":\"\",\"state\":\"NEW\",\"lastCheckTime\":0,\"rank\":0}", page.serialize().toString());
    }


}
