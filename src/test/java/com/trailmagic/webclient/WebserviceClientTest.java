package com.trailmagic.webclient;

import com.trailmagic.webclient.http.EntityContentProcessor;
import com.trailmagic.webclient.http.HttpFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.Reader;
import java.net.URISyntaxException;

import static org.junit.Assert.assertEquals;

public class WebserviceClientTest {
    private WebserviceClient webserviceClient;
    private StubWebServer webserver;

    @Before
    public void setUp() {
        webserviceClient = new WebserviceClient(new DefaultHttpClient(), new HttpFactory());
        webserver = new StubWebServer();
    }

    @After
    public void tearDown() throws Exception {
        webserver.stop();
    }

    @Test
    public void testGet() throws Exception {
        webserver.startOnPort(8080);
        webserver.registerUrl("/nothing", "content");

        assertEquals("content", webserviceClient.get("http://localhost:8080/nothing", new EntityContentProcessor<Object>() {
            @Override
            public Object process(Reader content) throws Exception {
                return IOUtils.toString(content);
            }
        }));
    }

    @Test(expected = RequestFailedException.class)
    public void testThrowsExceptionOnNon200Response() throws Exception {
        webserver.startOnPort(8080);
        webserver.registerUrl("/failure", 404);

        webserviceClient.get("http://localhost:8080/failure", new NoOpEntityContentProcessor());
    }

    @Test
    public void testPostsFile() throws Exception {
        webserver.startOnPort(8080);
        webserver.expectFilePostAtUrl("/postFile", exampleFile());

        webserviceClient.postFile("http://localhost:8080/postFile", exampleFile(), "application/octet-stream");

        webserver.assertExpectationsMet();
    }

    @Test
    public void testFollowsRedirect() throws Exception {
        webserver.startOnPort(8080);
        webserver.expectFilePostAtUrlAndFollowsRedirect("/postFile", exampleFile(), 303, "/newFile");

        webserviceClient.postFile("http://localhost:8080/postFile", exampleFile(), "application/octet-stream");

        webserver.assertExpectationsMet();

    }

    private File exampleFile() throws URISyntaxException {
        return new File(ClassLoader.getSystemResource("TEST_PORTRAIT.JPG").toURI());
    }

    @Test
    public void testThrowsRequestFailedOnProcessorException() {

    }

    private class NoOpEntityContentProcessor implements EntityContentProcessor<Object> {
        @Override
        public Object process(Reader content) throws Exception {
            return null;
        }
    }
}
