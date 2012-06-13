package org.motechproject.server.verboice.domain;

import org.apache.commons.lang.StringUtils;

public class VerboiceResponse extends AggregatedVerboiceAction {

    String callbackUrl;

    public VerboiceResponse(String callbackUrl) {
        this.callbackUrl = callbackUrl;
    }

    public VerboiceResponse() {
    }

    @Override
    public String toXMLString() {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("<Response>");
        buffer.append(super.toXMLString());
        if (StringUtils.isNotBlank(callbackUrl)) {
            buffer.append(new Redirect(callbackUrl).toXMLString());
        }
        buffer.append("</Response>");
        return buffer.toString();
    }
}
