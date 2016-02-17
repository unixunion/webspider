package com.deblox.web.handler;

import com.deblox.web.handler.impl.DxAuthProviderImpl;
import io.vertx.codegen.annotations.VertxGen;

/**
 * Created by keghol on 11/02/16.
 */
@VertxGen
public interface DxAuthProvider extends io.vertx.ext.auth.AuthProvider {

  String DEFAUT_USERNAME_FIELD = "username";

  static DxAuthProvider create() {
    return new DxAuthProviderImpl();
  }

  String getUsernameField();

}
