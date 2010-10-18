package com.trailmagic.webclient;

/**
 * Created by: oliver on Date: Oct 16, 2010 Time: 4:57:44 PM
 */
public class WebResponse {
    private boolean redirected;
    private String finalUrl;
    private int statusCode;

    public boolean isRedirected() {
        return redirected;
    }

    public String getFinalUrl() {
        return finalUrl;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setFinalUrl(String finalUrl) {
        this.finalUrl = finalUrl;
    }

    public void setRedirected(boolean redirected) {
        this.redirected = redirected;
    }

    @Override
    public String toString() {
        return "WebResponse {redirected=" + redirected + "; finalUrl=" + finalUrl + "; statusCode=" + statusCode + "}";
    }
}
