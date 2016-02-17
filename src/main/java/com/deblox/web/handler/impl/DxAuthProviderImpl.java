package com.deblox.web.handler.impl;

import com.deblox.myproject.Core;
import com.deblox.web.handler.DxAuthProvider;
import com.deblox.auth.DxUser;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.shareddata.impl.ClusterSerializable;
import io.vertx.ext.auth.User;

import javax.naming.AuthenticationException;

/**
 * AuthProvider Implementation which will request authorization and principal from "auth-address"
 * on the messagebus.
 *
 * Created by keghol on 11/02/16.
 */
public class DxAuthProviderImpl implements ClusterSerializable, DxAuthProvider {

  private static final Logger logger = LoggerFactory.getLogger(DxAuthProviderImpl.class);
  private String usernameField = DEFAUT_USERNAME_FIELD;
  private String authAddress = "auth-address";

  /**
   * authenticate against "auth-address"
   *
   * @param authInfo
   * @param resultHandler
   */
  @Override
  public void authenticate(JsonObject authInfo, Handler<AsyncResult<User>> resultHandler) {
    logger.info("Authenticate: " + authInfo.toString());

    String username = authInfo.getString("username");
    if (username == null) {
      resultHandler.handle(Future.failedFuture("authInfo must contain username in 'username' field"));
      return;
    }

    String password = authInfo.getString("password");
    if (password == null) {
      resultHandler.handle(Future.failedFuture("authInfo must contain password in 'password' field"));
      return;
    }

    Core.eb.send(authAddress, authInfo, resp -> {
      if (resp.succeeded()) {
        JsonObject authEvent = new JsonObject(resp.result().body().toString());
        if (authEvent.getString("status").equals("ok")) {
          // Instantiate the user
          DxUser dxUser = new DxUser(authEvent.getJsonObject("data"), this);
          resultHandler.handle(Future.succeededFuture(dxUser));

        } else {
          resultHandler.handle(Future.failedFuture(new AuthenticationException("Invalid Credentials")));
        }
      } else {
        logger.error("Unable to reach the auth service");
        resultHandler.handle(Future.failedFuture(new AuthenticationException("Auth System Unreachable")));
      }
    });
  }

  @Override
  public void writeToBuffer(Buffer buffer) {

  }

  @Override
  public int readFromBuffer(int pos, Buffer buffer) {
    return 0;
  }

  @Override
  public String getUsernameField() {
    return usernameField;
  }
}
