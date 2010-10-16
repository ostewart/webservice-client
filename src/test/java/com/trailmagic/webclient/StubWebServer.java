package com.trailmagic.webclient;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StubWebServer {
    private Server server;
    private MappingHandler handler = new MappingHandler();
    private List<Expectation> expectations = new ArrayList<Expectation>();

    public void startOnPort(int port) throws Exception {
        server = new Server(port);
        server.setHandler(handler);
        server.start();
    }

    public void registerUrl(String path, final String content) {
        handler.registerHandler(path, new SimpleHandler() {
            @Override
            public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
                IOUtils.write(content, response.getWriter());

            }
        });

    }

    public void stop() throws Exception {
        if (server != null) {
            server.stop();
        }
    }

    public void registerUrl(String path, final int statusCode) {
        handler.registerHandler(path, new SimpleHandler() {
            @Override
            public void handle(String s, HttpServletRequest request, HttpServletResponse response) {
                response.setStatus(statusCode);
            }
        });

    }

    public void expectFilePostAtUrl(String path, final File file) {
        final Expectation filePosted = new Expectation("File post to " + path);
        final Expectation fileContentsMatch = new Expectation("Posted file contents match at url: " + path);
        expectations.add(filePosted);
        expectations.add(fileContentsMatch);

        handler.registerHandler(path, new SimpleHandler() {
            @Override
            public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
                filePosted.setSatisfied(true);

                fileContentsMatch.setSatisfied(IOUtils.contentEquals(new FileInputStream(file), request.getInputStream()));
            }
        });
    }

    public void assertExpectationsMet() {
        if (expectations.isEmpty()) {
            throw new ExpectationFailedException("No expectations were set");
        }
        for (Expectation expectation : expectations) {
            if (!expectation.isSatisfied()) {
                throw new ExpectationFailedException("Failed " + expectation.toString());
            }
        }
    }

    public void expectFilePostAtUrlAndFollowsRedirect(String path, final File file, final int redirectCode, final String redirectPath) {
        final Expectation filePosted = new Expectation("File post to " + path);
        final Expectation fileContentsMatch = new Expectation("Posted file contents match at url: " + path);
        expectations.add(filePosted);
        expectations.add(fileContentsMatch);

        handler.registerHandler(path, new SimpleHandler() {
            @Override
            public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
                filePosted.setSatisfied(true);
                fileContentsMatch.setSatisfied(IOUtils.contentEquals(new FileInputStream(file), request.getInputStream()));

                response.setStatus(redirectCode);
                response.addHeader("Location", response.encodeRedirectURL(urlFromPath(redirectPath)));
            }
        });

        final Expectation hitRedirect = new Expectation("Followed redirect to " + redirectPath);
        expectations.add(hitRedirect);
        handler.registerHandler(redirectPath, new SimpleHandler() {
            @Override
            public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException {
                hitRedirect.setSatisfied(true);
            }
        });

    }

    private String urlFromPath(String redirectPath) {
        return "http://localhost:" + server.getConnectors()[0].getPort() + redirectPath;
    }

    private class MappingHandler extends AbstractHandler {
        private Map<String, SimpleHandler> handlerMap = new HashMap<String, SimpleHandler>();

        @Override
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
            if (!handlerMap.containsKey(target)) {
                throw new RuntimeException("No handler registered for target:" + target);
            }

            handlerMap.get(target).handle(target, request, response);
            ((Request) request).setHandled(true);
        }

        public void registerHandler(String target, SimpleHandler handler) {
            handlerMap.put(target, handler);
        }
    }

    private interface SimpleHandler {
        public void handle(String target, HttpServletRequest request, HttpServletResponse response) throws IOException;
    }
}
