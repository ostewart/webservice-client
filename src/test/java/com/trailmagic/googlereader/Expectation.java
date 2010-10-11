package com.trailmagic.googlereader;

/**
 * Created by: oliver on Date: Oct 11, 2010 Time: 7:12:12 PM
 */
public class Expectation {
    private String description;
    private boolean satisfied;

    public Expectation(String description) {
        this.description = description;
    }

    public String toString() {
        return "Expectation: " + description;
    }

    public void setSatisfied(boolean satisfied) {
        this.satisfied = satisfied;
    }

    public boolean isSatisfied() {
        return satisfied;
    }
}
