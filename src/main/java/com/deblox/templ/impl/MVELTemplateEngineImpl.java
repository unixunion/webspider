package com.deblox.templ.impl;

import com.deblox.templ.MVELTemplateRegistry;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.Utils;
import com.deblox.templ.MVELTemplateEngine;
import io.vertx.ext.web.templ.impl.CachingTemplateEngine;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;
import org.mvel2.templates.TemplateRuntime;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MVELTemplateEngineImpl extends CachingTemplateEngine<CompiledTemplate> implements MVELTemplateEngine {

  private static final Logger logger = LoggerFactory.getLogger(MVELTemplateEngineImpl.class);
  MVELTemplateRegistry registry = new MVELTemplateRegistryImpl();


  public MVELTemplateEngineImpl() {
    super(DEFAULT_TEMPLATE_EXTENSION, DEFAULT_MAX_CACHE_SIZE);

    Vertx.vertx().fileSystem().readDir(DEFAULT_TEMPLATE_DIR, templateFiles-> {
      templateFiles.result().forEach(f -> {
        Path p = Paths.get(f);
        logger.info(p.getParent().getFileName());
        String fileName = p.getFileName().toString();
        logger.info("Compiling Template: " + fileName);
        Vertx.vertx().fileSystem().readFile(f, r -> {
          CompiledTemplate ct = TemplateCompiler.compileTemplate(r.result().toString());
          registry.addNamedTemplate(fileName, ct); //p.getParent().getFileName() + "/" +
        });
      });
    });

  }

  @Override
  public MVELTemplateEngine setExtension(String extension) {
    doSetExtension(extension);
    return this;
  }

  @Override
  public MVELTemplateEngine setMaxCacheSize(int maxCacheSize) {
    this.cache.setMaxSize(maxCacheSize);
    return this;
  }

  @Override
  public void render(RoutingContext context, String templateFileName, Handler<AsyncResult<Buffer>> handler) {
    logger.info("Checking cache for template " + templateFileName);
    try {
      CompiledTemplate template = cache.get(templateFileName);
      if (template == null) {
        logger.info("Compiling Template: " + templateFileName);

        // real compile
        String loc = adjustLocation(templateFileName);

        String templateText = Utils.readFileToString(context.vertx(), loc);
        logger.info("Template Text: " + templateText);

        if (templateText == null) {
          throw new IllegalArgumentException("Cannot find template " + loc);
        }
        template = TemplateCompiler.compileTemplate(templateText);
        cache.put(templateFileName, template);
      }
      Map<String, RoutingContext> variables = new HashMap<>(1);
      variables.put("context", context);
      handler.handle(Future.succeededFuture(Buffer.buffer((String)TemplateRuntime.execute(template, context, variables, registry))));
    } catch (Exception ex) {
      handler.handle(Future.failedFuture(ex));
    }
  }


}