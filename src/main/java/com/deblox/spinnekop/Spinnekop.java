package com.deblox.spinnekop;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spinnekop {

    private static final Logger logger = LoggerFactory.getLogger(Spinnekop.class);

    Vertx vertx;
    WebClient httpClient;
    static XPathFactory xpfactory = XPathFactory.newInstance();
    static XPath xpath = xpfactory.newXPath();
    Pattern p = Pattern.compile("<a href=\"(.*?)\"");
    Pattern subContextOffCurrentMatch = Pattern.compile("^/.*");
    Pattern blacklist = Pattern.compile("^#.*");

    public Spinnekop(Vertx vertx) {
        this.vertx = vertx;
        WebClientOptions options = new WebClientOptions()
                .setUserAgent("PsimaxSearchBot/0.0.1");
        options.setKeepAlive(false);
        httpClient = WebClient.create(vertx, options);
    }

    public void getBody(String url, Handler<AsyncResult<Buffer>> handler) {
        logger.info("get: " + url);
        httpClient
                .getAbs(url)
                .send(resp -> {
                    if (resp.succeeded()) {
                        HttpResponse<Buffer> response = resp.result();
//                        resp.result().headers().forEach(h -> {
////                            logger.info(h.getKey() + ">:<" + h.getValue());
////                        });
                        handler.handle(Future.succeededFuture(response.body()));
                        } else {
                            handler.handle(Future.failedFuture(resp.cause()));
                        }
        });
    }

    public void getLinks(String url, String body, Handler<AsyncResult<List<String>>> handler) {
        try {
            Matcher m = p.matcher(body);
            List<String> links = new ArrayList<>();

            while (m.find()) {
                String link = m.group(1);

                Matcher blacklistMather = blacklist.matcher(link);
                Boolean blacklisted = blacklistMather.matches();

                if (!blacklisted) {
                    Matcher matcher = subContextOffCurrentMatch.matcher(link);
                    Boolean subContextMatch = matcher.matches();
                    if (subContextMatch) {
                        links.add(url + link);
                    } else {
                        links.add(m.group(1));
                    }
                }
            }

            handler.handle(Future.succeededFuture(links));
        } catch (Exception e) {
            handler.handle(Future.failedFuture(e));
        }
    }



}
