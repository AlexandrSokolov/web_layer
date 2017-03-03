package com.savdev.sse.rest.jeeasse;

import com.savdev.sse.consumer.SseConsumer;
import org.glassfish.jersey.media.sse.EventInput;
import org.glassfish.jersey.media.sse.EventSource;
import org.glassfish.jersey.media.sse.EventListener;
import org.glassfish.jersey.media.sse.InboundEvent;
import org.glassfish.jersey.media.sse.SseFeature;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.ScopeType;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import java.io.File;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
@RunWith(Arquillian.class)
public class SseServiceIT {

    public static final String base_dir = "basedir";
    public static final String web_inf_path = File.separator + "src" + File.separator + "main"
            + File.separator + "webapp" + File.separator + "WEB-INF" + File.separator;
    //both values can be found in src/test/resources/wildfly8x-resources/standalone-full-test.xml
    public static final int port_offset = 10000;
    public static final int http_port = 880;

    public static final String HTTP_URL = "http://127.0.0.1:" + (port_offset + http_port) +
            JAXRSConfiguration.CONTEXT_ROOT + JAXRSConfiguration.APPLICATION_PATH + SseService.URL;

    @Deployment
    public static WebArchive createDeployment() {
        ResourceBundle resourceBundle = ResourceBundle.getBundle(base_dir);
        //after building in maven, looks like: basedir=/home/alexandr/projects/web_layer/sse_rest_jeasse
        String baseDir = resourceBundle.getString("basedir");
        File[] files = Maven.resolver().loadPomFromFile(baseDir + File.separator + "pom.xml")
                .importDependencies(ScopeType.COMPILE).resolve().withTransitivity().asFile();

        WebArchive war = ShrinkWrap.create(WebArchive.class, "sse_rest_jeasse.war")
                .addPackage(Package.getPackage("com.savdev.sse.rest.jeeasse"))
                .addClass(SseConsumer.class)
                .addAsLibraries(files)
                .addAsWebInfResource(Paths.get(baseDir + web_inf_path + "jboss-deployment-structure.xml").toFile())
                .addAsWebInfResource(Paths.get(baseDir + web_inf_path + "jboss-web.xml").toFile())
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        System.out.println(war.toString(true));
        return war;
    }

    @Test
    @RunAsClient
    public void testSseJerseyClientPoolModel() {
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        Client client = ClientBuilder.newBuilder().register(SseFeature.class).build();
        WebTarget target = client.target(HTTP_URL);
        EventInput eventInput = target.request().get(EventInput.class);
        //wait for the 4 messages
        while (!eventInput.isClosed() && atomicInteger.get() < 4) {
            final InboundEvent inboundEvent = eventInput.read();
            if (inboundEvent == null) {
                // connection has been closed
                break;
            }
            System.out.println(inboundEvent.getName() + "; "
                    + inboundEvent.readData(String.class));
            atomicInteger.incrementAndGet();
        }
        eventInput.close();
        //if we here, everything worked fine, we got
    }

    @Test
    @RunAsClient
    public void testSseJerseyClientPushModel() throws InterruptedException {
        Client client = ClientBuilder.newBuilder()
                .register(SseFeature.class).build();
        WebTarget target = client.target(HTTP_URL);
        EventSource eventSource = EventSource.target(target).build();
        final AtomicInteger atomicInteger = new AtomicInteger(0);
        EventListener listener = (inboundEvent -> {
            System.out.println(inboundEvent.getName() + "; "
                    + inboundEvent.readData(String.class));
            atomicInteger.incrementAndGet();
        });
        eventSource.register(listener, "message");
        eventSource.open();
        while (atomicInteger.get() < 4) {
            Thread.sleep(4000);
        }
        eventSource.close();
    }
}
