
package com.deblox.web.templ;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.eventbus.Message;
import com.deblox.web.templ.impl.DxTemplateEngineImpl;
import io.vertx.ext.web.templ.TemplateEngine;

@VertxGen
public interface DxTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "templ";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static DxTemplateEngine create(String templateDir) {
    return new DxTemplateEngineImpl(templateDir);
  }

  /**
   * Set the extension for the engine
   *
   * @param extension  the extension
   * @return a reference to this for fluency
   */
  DxTemplateEngine setExtension(String extension);

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize  the maxCacheSize
   * @return a reference to this for fluency
   */
  DxTemplateEngine setMaxCacheSize(int maxCacheSize);


  void handleEventBus(Message<?> msg);

}