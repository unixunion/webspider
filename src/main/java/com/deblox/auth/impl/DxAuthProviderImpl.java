package com.deblox.auth.impl;

import com.deblox.auth.DxAuthProvider;
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

/**
 * Created by keghol on 11/02/16.
 */
public class DxAuthProviderImpl implements ClusterSerializable, DxAuthProvider {

  private static final Logger logger = LoggerFactory.getLogger(DxAuthProviderImpl.class);
  private String usernameField = DEFAUT_USERNAME_FIELD;

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

    User user = new DxUser(authInfo.getString("username"), this);
    resultHandler.handle(Future.succeededFuture(user));
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
