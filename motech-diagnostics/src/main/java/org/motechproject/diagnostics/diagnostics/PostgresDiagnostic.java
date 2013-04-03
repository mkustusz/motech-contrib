package org.motechproject.diagnostics.diagnostics;

import org.motechproject.diagnostics.Diagnostics;
import org.motechproject.diagnostics.annotation.Diagnostic;
import org.motechproject.diagnostics.response.DiagnosticsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static org.hibernate.exception.ExceptionUtils.getFullStackTrace;
import static org.motechproject.diagnostics.response.Status.Fail;
import static org.motechproject.diagnostics.response.Status.Success;

@Component
public class PostgresDiagnostic implements Diagnostics {

    private Properties postgresProperties;

    @Autowired(required = false)
    public void setPostgresProperties(@Qualifier("postgresProperties") Properties postgresProperties) {
        this.postgresProperties = postgresProperties;
    }

    @Diagnostic(name = "Can create connection")
    public DiagnosticsResult performDiagnosis() {
        Connection connection;
        try {
            connection = getConnection();
            if (connection != null)
                connection.close();
        } catch (SQLException e) {
            return new DiagnosticsResult("POSTGRES DATABASE CONNECTION EXCEPTION", getFullStackTrace(e), Fail);
        }
        return new DiagnosticsResult("POSTGRES DATABASE CONNECTION OPENED", "true", Success);
    }

    protected Connection getConnection() throws SQLException {
        String url = postgresProperties.getProperty("jdbc.url");
        String userName = postgresProperties.getProperty("jdbc.username");
        String password = postgresProperties.getProperty("jdbc.password");
        return DriverManager.getConnection(url, userName, password);
    }

    @Override
    public String name() {
        return DiagnosticServiceName.POSTGRES;
    }

    @Override
    public boolean canPerformDiagnostics() {
        return postgresProperties != null;
    }
}