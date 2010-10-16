package com.trailmagic.webclient;

/**
 * Created by: oliver on Date: Oct 16, 2010 Time: 4:57:44 PM
 */
public class WebResponse {
    private boolean redirected;
    private String url;
    private int statusCode;

    public boolean isRedirected() {
        return redirected;
    }

    public String getUrl() {
        return url;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }
}
