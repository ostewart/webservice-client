package com.trailmagic.googlereader;

/**
 * Created by: oliver on Date: Dec 14, 2009 Time: 8:34:01 PM
 */
public class RequestFailedException extends RuntimeException {
    public RequestFailedException(String feedUrl, Exception e) {
        super(feedUrl, e);
    }

    public RequestFailedException(String feedUrl, int statusCode) {
        super("Loading " + feedUrl + " failed with HTTP " + statusCode);
    }
}
