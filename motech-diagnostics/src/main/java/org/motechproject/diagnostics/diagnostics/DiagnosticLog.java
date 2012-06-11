package org.motechproject.diagnostics.diagnostics;

import org.hibernate.exception.ExceptionUtils;

public class DiagnosticLog {

    private String entity;

    private StringBuilder log = new StringBuilder();

    public DiagnosticLog(String entity) {
        this.entity = entity;
    }

    public void add(String message) {
        log.append(message + "\n");
    }

    @Override
    public String toString() {
        return "\n" + "#--------------------------# "
                + entity + " #-------------------------#" + "\n\n" + log;
    }

    public void addError(Exception e) {
        log.append("EXCEPTION: " + ExceptionUtils.getFullStackTrace(e)+"\n\n");
    }
}
