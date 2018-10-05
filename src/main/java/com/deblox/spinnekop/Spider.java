package com.deblox.spinnekop;

import com.deblox.Util;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Spider extends AbstractVerticle implements Handler<Message> {

    private static final Logger logger = LoggerFactory.getLogger(Spider.class);
    public static EventBus eb;
    Properties props = new Properties();
    Connection db;
    Spinnekop spinnekop;
    boolean databasePersist = false;
    boolean filePersist = true;

    List<SPage> pageQueue;
    boolean dequeuing = false;

    int credits = 20;

    @Override
    public void start(final Future<Void> startFuture) {

        logger.info("Starting up with Config: " + config().toString());
        eb = vertx.eventBus();
        eb.consumer("manager-address", message -> {
            handle(message);
        });

        props.setProperty("user", "spinnekopuser");
        props.setProperty("sslmode", "disable");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            startFuture.fail(e);
        }

        try {
            db = DriverManager
                    .getConnection("jdbc:postgresql://127.0.0.1:26257/spinnekop", props);
        } catch (SQLException e) {
            e.printStackTrace();
            startFuture.fail(e);
        }

        try {
            db.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS pages_v2 (" +
                            "id INT PRIMARY KEY, " +
                            "parent INT," +
                            "url STRING UNIQUE, " +
                            "body JSONB, " +
                            "state INT, " +
                            "last_check_time DATE)"
            );
        } catch (SQLException e) {
            startFuture.fail(e);
            System.exit(1);
        }

        spinnekop = new Spinnekop(vertx);
        pageQueue = new ArrayList<>();

        vertx.setPeriodic(1000, dequeue -> {
            logger.info(pageQueue.size());
            if (!dequeuing) {
                dequeue();
            }
        });

        startFuture.complete();
        logger.info("Startup Complete");

        vertx.setTimer(1000, t -> {
            vertx.eventBus().send("manager-address", new JsonObject()
                            .put("action", "spider")
                            .put("url", "https://stackoverflow.com"), resp -> {
                logger.info("done");
            });
        });

    }


    public void dequeue() {
        dequeuing = true;
        pageQueue.forEach(p -> {
            if (credits>0) {
                credits--;
                logger.info("requesting: " + p.getUrl());
                vertx.eventBus().send("manager-address", new JsonObject().put("action", "spider").put("url", p.getUrl()), resp -> {
                    if (resp.succeeded()) {
                        pageQueue.remove(p);
                    } else {
                        logger.error("error :" + p.getUrl());
                    }
                    credits++;
                });
            } else {
                logger.debug("max concurrency achieved");
            }
        });
        dequeuing = false;
    }


    public void get(Message event) {
        JsonObject request = new JsonObject(event.body().toString());
        SPage page = new SPage(request.getString("url"));
        String hash = null;
        try {
            hash = StringHasher.sha1(page.getUrl());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        logger.info("Spider: " + page.getUrl() + " cachefile: " + String.format("testdata/%s.txt", hash));

        String spageStr = null;

        try {
            spageStr = Util.readStringFromFile(String.format("testdata/%s.txt", hash));
            SPage cachedPage = SPage.fromJson(new JsonObject(spageStr));
            if (cachedPage.getSublinks().size()>0) {
                logger.info("cached page");
                cachedPage.getSublinks().forEach(link -> {
                    logger.info("link: " + link);
                    pageQueue.add(new SPage(link));
                });
                event.reply("cached");
            }
        } catch (IOException e) {
            logger.warn("No such cache file");
        }

        try {

            logger.info("cache miss");
            spinnekop.getBody(page.getUrl(), bodyBuffer -> {

                if (bodyBuffer.succeeded()) {

                    page.setBody(new String(bodyBuffer.result().getBytes()));

                    spinnekop.getLinks(page.getUrl(), page.getBody(), resp->{
                        if (resp.succeeded()) {
                            page.setSublinks(resp.result());
                            page.getSublinks().forEach(link -> {
                                logger.info("link: " + link);
                                pageQueue.add(new SPage(link));
                            });

                            if (filePersist) {
                                vertx.eventBus().send("datacapture.service", page.serialize(), dsresp -> {
                                    logger.info("captured: " + request.getString("url"));
                                    event.reply("ok");
    //                                event.reply(new JsonObject().put("links", page.getSublinks()));
                                });
                            } else {
                                event.reply("ok");
    //                            event.reply(new JsonObject().put("links", page.getSublinks()));
                            }

                        } else {
                            logger.error("Error " + resp.cause());
                        }
                    });

                } else {
                    event.reply("fail");
                }
            });



        } catch (Exception e) {
            e.printStackTrace();
        }



    }


    @Override
    public void handle(Message event) {
        JsonObject request = new JsonObject(event.body().toString());
        switch (request.getString("action")) {
            case "ping":
                event.reply("ok");
                break;

            case "spider":
                get(event);
                break;

            default:
                event.reply("unknown action");
        }
    }


}
