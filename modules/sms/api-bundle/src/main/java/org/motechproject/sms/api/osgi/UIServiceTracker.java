package org.motechproject.sms.api.osgi;

import org.motechproject.osgi.web.ModuleRegistrationData;
import org.motechproject.osgi.web.UIFrameworkService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class UIServiceTracker extends ServiceTracker {

    private static Logger logger = LoggerFactory.getLogger(UIServiceTracker.class);

    private ModuleRegistrationData moduleRegistrationData;

    public UIServiceTracker(BundleContext context, ModuleRegistrationData moduleRegistrationData) {
        super(context, UIFrameworkService.class.getName(), null);
        this.moduleRegistrationData = moduleRegistrationData;
    }

    @Override
    public Object addingService(ServiceReference ref) {
        Object service = super.addingService(ref);
        serviceAdded((UIFrameworkService) service);
        return service;
    }

    @Override
    public void removedService(ServiceReference ref, Object service) {
        serviceRemoved((UIFrameworkService) service);
        super.removedService(ref, service);
    }

    private void serviceAdded(UIFrameworkService service) {
        service.registerModule(moduleRegistrationData);
        logger.debug(String.format("%s registered in UI framework", moduleRegistrationData.getModuleName()));
    }


    private void serviceRemoved(UIFrameworkService service) {
        service.unregisterModule(moduleRegistrationData.getModuleName());
        logger.debug(String.format("%s unregistered from ui framework", moduleRegistrationData.getModuleName()));
    }
}
