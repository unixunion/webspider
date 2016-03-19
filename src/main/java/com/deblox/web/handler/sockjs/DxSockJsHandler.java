/*
 * Copyright 2014 Red Hat, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package com.deblox.web.handler.sockjs;


import com.deblox.web.handler.sockjs.impl.DxSockJsHandlerImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;


@VertxGen
public interface DxSockJsHandler extends SockJSHandler {

  static DxSockJsHandlerImpl create(Vertx vertx, SockJSHandlerOptions options) {
    return new DxSockJsHandlerImpl(vertx, options);
  }


}