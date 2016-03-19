package com.deblox.auth;

import com.deblox.web.handler.DxAuthProvider;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AbstractUser;

/**
 * User model
 *
 * Created by keghol on 11/02/16.
 */

@VertxGen
public class DxUser extends AbstractUser {

  private static final Logger logger = LoggerFactory.getLogger(DxUser.class);
  private JsonObject principal;
  private DxAuthProvider dxAuth;

  public DxUser() {
  }

  public DxUser(String username, DxAuthProvider dxAuth) {
    this.principal = new JsonObject().put(dxAuth.getUsernameField(), username);
    this.dxAuth = dxAuth;
  }

  public DxUser(JsonObject principal, DxAuthProvider dxAuth) {
    this.principal = principal;
    this.dxAuth = dxAuth;
  }


  @Override
  protected void doIsPermitted(String permission, Handler<AsyncResult<Boolean>> resultHandler) {
    logger.info("doIsPermitted called for permission: " + permission);
    resultHandler.handle(Future.succeededFuture(this.principal().getBoolean(permission, false)));
  }

  @Override
  public JsonObject principal() {
    logger.info("get principal");
    return principal;
  }

  @Override
  public void setAuthProvider(io.vertx.ext.auth.AuthProvider authProvider) {
    logger.info("setAuthProvider");
    this.dxAuth = (DxAuthProvider) authProvider;
  }
}
