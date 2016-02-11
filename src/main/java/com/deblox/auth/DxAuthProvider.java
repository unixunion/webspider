package com.deblox.auth;

import com.deblox.auth.impl.DxAuthProviderImpl;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.ext.auth.AuthProvider;

/**
 * Created by keghol on 11/02/16.
 */
@VertxGen
public interface DxAuthProvider extends AuthProvider {

  String DEFAUT_USERNAME_FIELD = "username";

  static DxAuthProvider create() {
    return new DxAuthProviderImpl();
  }

  String getUsernameField();

}
