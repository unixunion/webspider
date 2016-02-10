package com.deblox.templ;

/**
 * Created by keghol on 10/02/16.
 */
import com.deblox.templ.impl.MVELTemplateRegistryImpl;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateRegistry;

import java.util.Iterator;
import java.util.Set;

public interface MVELTemplateRegistry extends TemplateRegistry {
  Iterator iterator();

  Set<String> getNames();

  boolean contains(String name);

  void addNamedTemplate(String name, CompiledTemplate template);

  CompiledTemplate getNamedTemplate(String name);

  static MVELTemplateRegistry create() {
    return new MVELTemplateRegistryImpl();
  }
}