package com.deblox.web.templ.impl;

import com.deblox.web.templ.DxTemplateRegistry;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateError;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by keghol on 10/02/16.
 */
public class DxTemplateRegistryImpl implements DxTemplateRegistry {

  private static final Logger logger = LoggerFactory.getLogger(DxTemplateRegistryImpl.class);

  private Map<String, CompiledTemplate> NAMED_TEMPLATES = new HashMap<>();

  public void addNamedTemplate(String name, CompiledTemplate template) {
    NAMED_TEMPLATES.put(name, template);
  }

  public CompiledTemplate getNamedTemplate(String name) {
    CompiledTemplate t = NAMED_TEMPLATES.get(name);
    if (t == null) throw new TemplateError("no named template exists '" + name + "'");
    return t;
  }

  public Iterator iterator() {
    return NAMED_TEMPLATES.keySet().iterator();
  }

  public Set<String> getNames() {
    return NAMED_TEMPLATES.keySet();
  }

  public boolean contains(String name) {
    return NAMED_TEMPLATES.containsKey(name);
  }
}
