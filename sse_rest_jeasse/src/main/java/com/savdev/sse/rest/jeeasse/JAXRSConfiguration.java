package com.savdev.sse.rest.jeeasse;

import com.google.common.collect.Sets;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.core.Application;
import java.util.Set;

/**
 *
 */
@ApplicationPath(JAXRSConfiguration.APPLICATION_PATH)
public class JAXRSConfiguration extends Application {

    //see src/main/webapp/WEB-INF/jboss-web.xml
    public static final String CONTEXT_ROOT = "/sse";
    public static final String APPLICATION_PATH = "/rest";

    @Override
    public Set<Class<?>> getClasses() {
        return Sets.newHashSet(SseService.class);
    }
}
