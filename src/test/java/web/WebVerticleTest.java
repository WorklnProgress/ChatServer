package web;

/**
 * Created by priye on 5/13/17.
 */

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * This is our JUnit test for our verticle. The test uses vertx-unit, so we declare a custom runner.
 */
@RunWith(VertxUnitRunner.class)
public class WebVerticleTest {

    private Vertx vertx;
    private Integer port;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions()
                .setConfig(new JsonObject()
                        .put("http.port", port)
                        .put("username", "root")
                        .put("password", "PWD123!")
                        .put("host", System.getProperty("db.host", "localhost"))
                        .put("queryTimeout", Long.parseLong(System.getProperty("db.queryTimeout", "10000"))
                        ));

        // We pass the options as the second parameter of the deployVerticle method.
        vertx.deployVerticle(WebVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    /**
     * This method, called after our test, just cleanup everything by closing the vert.x instance
     *
     * @param context the test context
     */
    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    /**
     * Let's ensure that our application behaves correctly.
     *
     * @param context the test context
     */
    @Test
    public void testChatServer(TestContext context) {
        final Async async = context.async();
        //default page should have "Hello"
        vertx.createHttpClient().getNow(port, "localhost", "/", response -> {
            response.handler(body -> {
                context.assertTrue(body.toString().contains("Hello"));
                async.complete();
            });
        });
    }

    @Test
    public void checkThatTheIndexPageIsServed(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/chat/index.html", response -> {
            context.assertEquals(response.statusCode(), 200);
            context.assertEquals(response.headers().get("content-type"), "text/html;charset=UTF-8");
            response.bodyHandler(body -> {
                context.assertTrue(body.toString().contains("<title>Chat Client</title>"));
                async.complete();
            });
        });
    }

    @Test
    public void checkThatWeCanGet(TestContext context) {
        Async async = context.async();
        HttpClientRequest req = vertx.createHttpClient().get(port, "localhost", "/api/getUsers");
        req.handler(response -> {
            context.assertEquals(response.statusCode(), 200);
            async.complete();
        });
        req.end();
    }

    @Test
    public void testDoNothing(TestContext ctx){

    }
}