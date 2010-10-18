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
import static org.junit.Assert.assertTrue;

public class WebserviceClientTest {
    private WebserviceClient webserviceClient;
    private StubWebServer webserver;
    private static final int SERVER_PORT = 8090;
    private static final String URL_BASE = "http://localhost:" + SERVER_PORT;
    private static final int HTTP_SEE_OTHER = 303;
    private static final int HTTP_NOT_FOUND = 404;

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
        webserver.startOnPort(SERVER_PORT);
        webserver.registerUrl("/nothing", "content");

        assertEquals("content", webserviceClient.get(URL_BASE + "/nothing", new EntityContentProcessor<Object>() {
            @Override
            public Object process(Reader content) throws Exception {
                return IOUtils.toString(content);
            }
        }));
    }

    @Test(expected = RequestFailedException.class)
    public void testThrowsExceptionOnNon200Response() throws Exception {
        webserver.startOnPort(SERVER_PORT);
        webserver.registerUrl("/failure", HTTP_NOT_FOUND);

        webserviceClient.get(URL_BASE + "/failure", new NoOpEntityContentProcessor());
    }

    @Test
    public void testPostsFile() throws Exception {
        webserver.startOnPort(SERVER_PORT);
        webserver.expectFilePostAtUrl("/postFile", exampleFile());

        webserviceClient.postFile(URL_BASE + "/postFile", exampleFile(), "application/octet-stream");

        webserver.assertExpectationsMet();
    }

    @Test
    public void testFollowsRedirectAndPopulatesResponseObject() throws Exception {
        webserver.startOnPort(SERVER_PORT);
        webserver.expectFilePostAtUrlAndFollowsRedirect("/postFile", exampleFile(), HTTP_SEE_OTHER, "/newFile");

        WebResponse response = webserviceClient.postFile(URL_BASE + "/postFile", exampleFile(), "application/octet-stream");

        webserver.assertExpectationsMet();
        assertTrue(response.isRedirected());
        assertEquals(URL_BASE + "/newFile", response.getFinalUrl());
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
