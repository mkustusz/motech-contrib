package org.motechproject.server.verboice.domain;

public class Redirect implements VerboiceAction {
    private String url;

    public Redirect(String url) {
        this.url = url;
    }

    @Override
    public String toXMLString() {
        return "<Redirect method=\"POST\">" + url + "</Redirect>";
    }
}
