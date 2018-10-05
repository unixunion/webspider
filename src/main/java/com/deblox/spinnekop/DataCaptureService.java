package com.deblox.spinnekop;

import com.deblox.Util;
import io.vertx.core.*;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class DataCaptureService extends AbstractVerticle implements Handler<Message<JsonObject>> {

    private static final Logger logger = LoggerFactory.getLogger(DataCaptureService.class);
    private EventBus eb;

    public void start(Future<Void> startFuture) throws Exception {
        logger.info("Starting up");
        eb = vertx.eventBus();
        eb.consumer("datacapture.service", this);
        startFuture.complete();
    }

    @Override
    public void handle(Message<JsonObject> event) {
        logger.info("Capturing content: " + event.body().getString("url"));
        try {
            journalMsg(event.body(), resp -> {
                event.reply("ok");
            });
        } catch (Exception e) {
            e.printStackTrace();
            event.fail(500, "fail");
        }
    }

    private void journalMsg(JsonObject page, Handler<AsyncResult<Void>> handler) {
        try {
            String hash = StringHasher.sha1(page.getString("url"));
            Util.writeStringToFile(page.toString(), String.format("testdata/%s.txt", hash), resp -> {
                handler.handle(Future.succeededFuture());
            });
        } catch (Exception e) {
            e.printStackTrace();
            handler.handle(Future.failedFuture(e.getMessage()));
        }
    }

}
