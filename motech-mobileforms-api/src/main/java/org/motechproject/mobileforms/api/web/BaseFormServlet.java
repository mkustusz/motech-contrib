package org.motechproject.mobileforms.api.web;

import org.fcitmuk.epihandy.EpihandyXformSerializer;
import org.motechproject.mobileforms.api.callbacks.FormGroupPublisher;
import org.motechproject.mobileforms.api.callbacks.FormParser;
import org.motechproject.mobileforms.api.domain.FormBean;
import org.motechproject.mobileforms.api.domain.FormGroupValidator;
import org.motechproject.mobileforms.api.domain.FormOutput;
import org.motechproject.mobileforms.api.parser.FormDataParser;
import org.motechproject.mobileforms.api.repository.AllMobileForms;
import org.motechproject.mobileforms.api.service.MobileFormsService;
import org.motechproject.mobileforms.api.service.UsersService;
import org.motechproject.mobileforms.api.utils.MapToBeanConvertor;
import org.motechproject.mobileforms.api.validator.FormValidator;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class BaseFormServlet extends HttpServlet {

    public static final byte RESPONSE_ERROR = 0;
    public static final byte RESPONSE_SUCCESS = 1;

    public static final String FAILED_TO_SERIALIZE_DATA = "failed to serialize data";
    public static final String APPLICATION_OCTET_STREAM = "application/octet-stream";

    protected UsersService usersService;
    protected ApplicationContext context;
    protected FormGroupPublisher formGroupPublisher;
    protected MobileFormsService mobileFormsService;
    protected AllMobileForms allMobileForms;
    protected String marker;
    protected FormGroupValidator formGroupValidator;


    protected BaseFormServlet() {
        context = new ClassPathXmlApplicationContext("applicationMobileFormsAPI.xml");
        mobileFormsService = context.getBean("mobileFormsServiceImpl", MobileFormsService.class);
        usersService = context.getBean("usersServiceImpl", UsersService.class);
        formGroupPublisher = context.getBean("formGroupPublisher", FormGroupPublisher.class);
        allMobileForms = context.getBean("allMobileForms", AllMobileForms.class);
        marker = context.getBean("mobileFormsProperties", Properties.class).getProperty("forms.xml.form.name");
        formGroupValidator = new FormGroupValidator();
    }

    protected EpihandyXformSerializer serializer() {
        return new EpihandyXformSerializer();
    }

    protected void readParameters(DataInputStream dataInput) throws IOException {
        String name = dataInput.readUTF();
        String password = dataInput.readUTF();
        String serializer = dataInput.readUTF();
        String locale = dataInput.readUTF();
    }

    @Override
    protected abstract void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException;

    protected Map<String, FormValidator> getFormValidators() {
        Map<String, FormValidator> validators = new HashMap<String, FormValidator>();
        final Enumeration attributeNames = getServletContext().getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            final String attributeName = (String) attributeNames.nextElement();
            final Object attributeValue = getServletContext().getAttribute(attributeName);
            if (attributeValue instanceof FormValidator) {
                validators.put(attributeName, (FormValidator) attributeValue);
            }
        }
        return validators;
    }

    protected FormValidator getValidatorFor(FormBean formBean) {
        return (FormValidator) getServletContext().getAttribute(formBean.getValidator());
    }

    protected byte readActionByte(DataInputStream dataInput) throws IOException {
        return dataInput.readByte();
    }

    protected FormOutput getFormOutput() {
        return new FormOutput();
    }

    protected FormParser createFormProcessor() {
        return new FormParser(new FormDataParser(), new MapToBeanConvertor(), allMobileForms, marker);
    }
}
