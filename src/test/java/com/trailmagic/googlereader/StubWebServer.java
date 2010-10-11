package com.trailmagic.googlereader;

import org.apache.commons.io.IOUtils;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StubWebServer {
    private Server server;
    private MappingHandler handler = new MappingHandler();

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
