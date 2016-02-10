package com.deblox.templating;

/**
 * Created by keghol on 10/02/16.
 */
import com.deblox.templating.impl.DxTemplateRegistryImpl;
import org.mvel2.templates.CompiledTemplate;
import org.mvel2.templates.TemplateRegistry;

import java.util.Iterator;
import java.util.Set;

public interface DxTemplateRegistry extends TemplateRegistry {
  Iterator iterator();

  Set<String> getNames();

  boolean contains(String name);

  void addNamedTemplate(String name, CompiledTemplate template);

  CompiledTemplate getNamedTemplate(String name);

  static DxTemplateRegistry create() {
    return new DxTemplateRegistryImpl();
  }
}