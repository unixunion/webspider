package com.deblox.web.handler;


import com.deblox.web.handler.impl.DxBlogHandlerImpl;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by keghol on 17/02/16.
 */
public interface DxBlogHandler extends Handler<RoutingContext> {


  static DxBlogHandler create() { return new DxBlogHandlerImpl(); }
//  user.isAuthorised("printers:printer1234", res -> {
//    if (res.succeeded()) {
//
//      boolean hasAuthority = res.result();
//
//      if (hasAuthority) {
//        System.out.println("User has the authority");
//      } else {
//        System.out.println("User does not have the authority");
//      }
//
//    } else {
//      res.cause().printStackTrace();
//    }

}

