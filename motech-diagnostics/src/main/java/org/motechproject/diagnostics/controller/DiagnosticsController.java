package org.motechproject.diagnostics.controller;

import org.motechproject.diagnostics.DiagnosticResponseBuilder;
import org.motechproject.diagnostics.response.DiagnosticsResult;
import org.motechproject.diagnostics.response.ExceptionResponse;
import org.motechproject.diagnostics.repository.AllDiagnosticMethods;
import org.motechproject.diagnostics.response.ServiceResult;
import org.motechproject.diagnostics.service.DiagnosticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@RequestMapping(value = "/diagnostics/")
public class DiagnosticsController {

    DiagnosticsService diagnosticsService;
    private DiagnosticResponseBuilder diagnosticResponseBuilder;

    @Autowired
    public DiagnosticsController(DiagnosticsService diagnosticsService, DiagnosticResponseBuilder diagnosticResponseBuilder) {
        this.diagnosticsService = diagnosticsService;
        this.diagnosticResponseBuilder = diagnosticResponseBuilder;
    }

    @RequestMapping(value = "service/{serviceName}", method = GET)
    @ResponseBody
    public ServiceResult runDiagnostics(@PathVariable("serviceName") String name) throws IOException {
        ServiceResult serviceResult = diagnosticsService.run(name);
        return serviceResult;
    }

    @RequestMapping(value = "all", method = GET)
    @ResponseBody
    public List<ServiceResult> getDiagnostics() throws InvocationTargetException, IllegalAccessException, IOException {
        List<ServiceResult> serviceResults = diagnosticsService.runAll();
        return serviceResults;
    }

    @RequestMapping(value = "all/view", method = GET)
    public void viewDiagnostics(HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {
        String diagnosticsResponse = diagnosticResponseBuilder.createResponseMessage(getDiagnostics());
        response.getOutputStream().print(diagnosticsResponse);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public String handleException(final Exception exception, HttpServletResponse response) {
        response.setHeader("Content-Type", "application/json");
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        StringWriter stringWriter = new StringWriter();
        exception.printStackTrace(new PrintWriter(stringWriter));
        return new ExceptionResponse(exception.getMessage(), stringWriter.toString()).toString();
    }
}
