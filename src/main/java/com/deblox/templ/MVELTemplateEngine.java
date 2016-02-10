
package com.deblox.templ;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.web.templ.TemplateEngine;
import com.deblox.templ.impl.MVELTemplateEngineImpl;

@VertxGen
public interface MVELTemplateEngine extends TemplateEngine {

  /**
   * Default max number of templates to cache
   */
  int DEFAULT_MAX_CACHE_SIZE = 10000;

  /**
   * Default template extension
   */
  String DEFAULT_TEMPLATE_EXTENSION = "templ";

  /**
   * Default templates location
   */
  String DEFAULT_TEMPLATE_DIR = "templates";

  /**
   * Create a template engine using defaults
   *
   * @return  the engine
   */
  static MVELTemplateEngine create() {
    return new MVELTemplateEngineImpl();
  }

  /**
   * Set the extension for the engine
   *
   * @param extension  the extension
   * @return a reference to this for fluency
   */
  MVELTemplateEngine setExtension(String extension);

  /**
   * Set the max cache size for the engine
   *
   * @param maxCacheSize  the maxCacheSize
   * @return a reference to this for fluency
   */
  MVELTemplateEngine setMaxCacheSize(int maxCacheSize);
}