package com.savdev.sse.rest.jeeasse;

import info.macias.sse.EventBroadcast;
import info.macias.sse.servlet3.ServletEventTarget;

import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

/**
 *
 */
@Path(SseService.URL)
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class SseService {

    public static final String URL = "/jeasse";

    @Context
    private HttpServletRequest servletRequest;

    EventBroadcast broadcaster = new EventBroadcast();

    @GET
    public void establishConnection() throws Exception {
        broadcaster.addSubscriber(new ServletEventTarget(servletRequest));
    }

    public void notifySubscribers(String message){
        broadcaster.broadcast("message", message);
    }
}
