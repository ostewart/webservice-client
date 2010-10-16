package com.trailmagic.webclient;

import com.trailmagic.webclient.http.EntityContentProcessor;
import com.trailmagic.webclient.http.HttpFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.FileEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class WebserviceClient {
    private static final Logger log = LoggerFactory.getLogger(WebserviceClient.class);
    private HttpFactory httpFactory;
    private HttpClient httpClient;

    @Autowired
    public WebserviceClient(HttpClient httpClient, HttpFactory httpFactory) {
        this.httpClient = httpClient;
        this.httpFactory = httpFactory;
    }

    public <T> T get(String url, EntityContentProcessor<T> processor) throws RequestFailedException {
        HttpGet get = httpFactory.get(url);
        log.debug("GET request: {}", url);
        try {
            HttpResponse response = httpClient.execute(get);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                log.debug("Got response for feed: {}, processing content", url);
                HttpEntity httpEntity = response.getEntity();
                InputStream content = httpEntity.getContent();
                try {
                    InputStreamReader contentReader = new InputStreamReader(content);
                    try {
                        T result = processor.process(contentReader);
                        log.debug("Finished processing content for: {}", url);
                        return result;
                    } finally {
                        contentReader.close();
                    }
                } finally {
                    content.close();
                    consumeContent(response);
                }
            } else {
                log.warn("Error response for GET {}, response body said: {}",
                         url, EntityUtils.toString(response.getEntity()));
                throw new RequestFailedException(url, response.getStatusLine().getStatusCode());
            }
        } catch (Exception e) {
            throw new RequestFailedException(url, e);
        }
    }

    private void consumeContent(HttpResponse response) throws IOException {
        HttpEntity httpEntity = response.getEntity();
        if (httpEntity != null) {
            httpEntity.consumeContent();
        }
    }

    public int post(String url, Map<String, String> bodyParams) {
        HttpPost post = httpFactory.post(url);
        post.setEntity(httpFactory.urlEncodedFormEntity(mapToNameValuePairList(bodyParams)));

        return executePost(post);
    }

    private int executePost(HttpPost post) {
        try {
            HttpResponse response = httpClient.execute(post);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                httpEntity.consumeContent();
            }

            return response.getStatusLine().getStatusCode();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<BasicNameValuePair> mapToNameValuePairList(Map<String, String> bodyParams) {
        ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
        for (String key : bodyParams.keySet()) {
            pairs.add(new BasicNameValuePair(key, bodyParams.get(key)));
        }
        return pairs;
    }

    public void setCookieWhenNotTesting(BasicClientCookie authCookie) {
        if (httpClient instanceof DefaultHttpClient) {
            ((DefaultHttpClient) httpClient).getCookieStore().addCookie(authCookie);
        } else {
            log.warn("Not setting cookie {}; httpClient is not a DefaultHttpClient", authCookie);
        }
    }

    public void postFile(String url, File file, String contentType) {
        final HttpPost post = httpFactory.post(url);
        post.setEntity(new FileEntity(file, contentType));
        
        executePost(post);
    }
}