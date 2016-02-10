package com.deblox.templating.impl;

import com.deblox.Boot;
import com.deblox.myproject.PingVerticle;
import com.deblox.templating.DxTemplateRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import com.deblox.templating.DxTemplateEngine;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRuntime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DxTemplateEngineImpl extends CachingTemplateEngine<CompiledTemplate> implements DxTemplateEngine {

  private static final Logger logger = LoggerFactory.getLogger(DxTemplateEngineImpl.class);
  DxTemplateRegistry registry = new DxTemplateRegistryImpl();

  public DxTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);
    logger.info("Creating MVELTemplateEngine instance");
    PingVerticle.vx.fileSystem().readDir(DEFAULT_TEMPLATE_DIR, templateFiles-> {
      templateFiles.result().forEach(f -> {
        Path p = Paths.get(f);
        logger.info(p.getParent().getFileName());
        String fileName = p.getFileName().toString();
        logger.info("Compiling Template: " + fileName);
        PingVerticle.vx.fileSystem().readFile(f, r -> {
          try {
            CompiledTemplate ct = TemplateCompiler.compileTemplate(r.result().toString());
            logger.info("Registering template: " + fileName);
            registry.addNamedTemplate(fileName, ct); //p.getParent().getFileName() + "/" +
          } catch (Exception e) {
            logger.warn("Skipping due to error " + f);
          }
        });
      });
    });

    logger.info("Registering hander on " + this.getClass().getSimpleName());
    PingVerticle.eb.consumer(this.getClass().getSimpleName(), res -> {
      logger.info("Event Processing");
      this.handleEventBus((Message<?>) res);
    });

  }

  @Override
  public DxTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public DxTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void handleEventBus(Message<?> msg) {
    logger.info("msg: " + msg.body().toString());
    msg.reply("Acknowledged");
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    try {
      CompiledTemplate template = cache.get(templateFileName);
      if (template == null) {
        logger.info("Compiling Template: " + templateFileName);

        // real compile
        String loc = adjustLocation(templateFileName);
        String templateText = Utils.readFileToString(context.vertx(), loc);

        if (templateText == null) {
          throw new IllegalArgumentException("Cannot find template " + loc);
        }
        template = TemplateCompiler.compileTemplate(templateText);
        cache.put(templateFileName, template);
      } else {
        logger.info("Cached template: " + templateFileName);
      }
      Map<String, RoutingContext> variables = new HashMap<>(1);
      variables.put("context", context);
      handler.handle(Future.succeededFuture(Buffer.buffer((String)TemplateRuntime.execute(template, context, variables, registry))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }


}