package org.motechproject.diagnostics;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.motechproject.diagnostics.response.DiagnosticsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.StringWriter;
import java.util.List;

@Component
public class DiagnosticResponseBuilder {

    private VelocityEngine velocityEngine;

    @Autowired
    public DiagnosticResponseBuilder(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    public String createResponseMessage(List<DiagnosticsResult> diagnosticsResponses) {
        Template template = velocityEngine.getTemplate("/diagnosticResponse.vm");
        VelocityContext context = new VelocityContext();
        context.put("diagnosticsResponses", diagnosticsResponses);
        StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}
