package com.deblox.myproject.unit.test;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.LDAPProviderConstants;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by keghol on 19/02/16.
 */

@RunWith(VertxUnitRunner.class)
public class ShiroAuthTest {

  Vertx vertx;
  EventBus eb;
  private static final Logger logger = LoggerFactory.getLogger(ShiroAuthTest.class);
  AuthProvider dxAuthProvider;

  @Before
  public void before(TestContext context) {
    logger.info("@Before");
    vertx = Vertx.vertx();
    eb = vertx.eventBus();
    dxAuthProvider = ShiroAuth.create(vertx, getOptions());
  }

  /**
   * return the ShiroAuthOptions
   *
   * @return
   */
  private  ShiroAuthOptions getOptions() {
    JsonObject shiroConfig = new JsonObject()
            .put(LDAPProviderConstants.LDAP_USER_DN_TEMPLATE_FIELD, "uid={0},dc=domain,dc=com")
            .put(LDAPProviderConstants.LDAP_URL, "ldap://ldapserver:389")
            .put(LDAPProviderConstants.LDAP_AUTHENTICATION_MECHANISM, "simple")
            .put(LDAPProviderConstants.LDAP_SYSTEM_USERNAME, "binduser")
            .put(LDAPProviderConstants.LDAP_SYSTEM_PASSWORD, "somepassword");

    ShiroAuthOptions shiroAuthOptions = new ShiroAuthOptions()
            .setType(ShiroAuthRealmType.LDAP)
            .setConfig(shiroConfig);

    return shiroAuthOptions;
  }

  @Test
  public void testAuth(TestContext test) {

    JsonObject authInfo = new JsonObject()
            .put("username", "keghol")
            .put("password", "password");

    Async async = test.async();

    dxAuthProvider.authenticate(authInfo, res -> {
      if (res.succeeded()) {
        logger.info("Success");
        logger.info(res.result().principal());
        async.complete();
      } else {
        logger.error("Nope, failure");
        logger.error(res.result());
        test.fail();
      }
    });

  }
}

