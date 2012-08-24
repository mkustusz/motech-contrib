package org.motechproject.diagnostics.repository;

import org.motechproject.diagnostics.annotation.Diagnostic;
import org.motechproject.diagnostics.model.DiagnosticMethod;
import org.motechproject.diagnostics.response.DiagnosticsResult;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Repository;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.motechproject.diagnostics.model.DiagnosticMethod.isValidDiagnosticMethod;

@Repository
public class AllDiagnosticMethods implements BeanPostProcessor {

    List<DiagnosticMethod> diagnosticMethods = new ArrayList<DiagnosticMethod>();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        for (Method method : bean.getClass().getDeclaredMethods())
            if (isValidDiagnosticMethod(method))
                diagnosticMethods.add(new DiagnosticMethod(method.getAnnotation(Diagnostic.class).name(), bean, method));
        return bean;
    }

    public List<DiagnosticsResult> runAllDiagnosticMethods() throws InvocationTargetException, IllegalAccessException {
        List<DiagnosticsResult> diagnosticsResponses = new ArrayList<DiagnosticsResult>();
        for (DiagnosticMethod diagnosticMethod : diagnosticMethods) {
            DiagnosticsResult response = diagnosticMethod.run();
            if (response != null)
                diagnosticsResponses.add(response);
        }
        return diagnosticsResponses;
    }
}