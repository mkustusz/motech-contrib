package org.motechproject.server.config;

import org.apache.commons.io.IOUtils;
import org.motechproject.MotechException;
import org.motechproject.server.config.service.PlatformSettingsService;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SettingsFacade {

    private PlatformSettingsService platformSettingsService;

    private boolean rawConfigRegistered;
    private boolean propsRegistered;

    private BundleContext bundleContext;

    private Map<String, Properties> config = new HashMap<>();
    private Map<String, Resource> rawConfig = new HashMap<>();

    @Autowired(required = false)
    public void setPlatformSettingsService(PlatformSettingsService platformSettingsService) {
        this.platformSettingsService = platformSettingsService;
        if (!propsRegistered) {
            registerAllProperties();
        }
        if (!rawConfigRegistered) {
            registerAllRawConfig();
        }
    }

    @Autowired(required = false)
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    public void setConfigFiles(List<Resource> resources) {
        for (Resource configFile : resources) {
            InputStream is = null;
            try {
                is = configFile.getInputStream();

                Properties props = new Properties();
                props.load(is);

                config.put(getResourceFileName(configFile), props);
            } catch (IOException e) {
                throw new MotechException("Cant load config file " + configFile.getFilename(), e);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        registerAllProperties();
    }

    public void setRawConfigFiles(List<Resource> resources) {
        for (Resource resource : resources) {
            rawConfig.put(getResourceFileName(resource), resource);
        }
        registerAllRawConfig();
    }


    public String getProperty(String key) {
        String result = null;
        String filename = findFilename(key);

        if (filename != null) {
            result = getProperty(key, filename);
        }

        return result;
    }

    public String getProperty(String key, String filename) {
        String result = null;
        try {
            if (propsRegistered) {
                Properties props = platformSettingsService.getBundleProperties(getSymbolicName(), filename);
                config.put(filename, props);
            }

            Properties props = config.get(filename);
            if (props != null) {
                result = props.getProperty(key);
            }
        } catch (IOException e) {
            throw new MotechException("Can't read settings", e);
        }

        return result;
    }

    public Properties getProperties(String filename) {
        if (propsRegistered) {
            try {
                Properties props = platformSettingsService.getBundleProperties(getSymbolicName(), filename);
                config.put(filename, props);
            } catch (IOException e) {
                throw new MotechException("Can't read settings", e);
            }
        }

        Properties result = config.get(filename);
        return (result == null ? new Properties() : result);
    }

    public void setProperty(String filename, String key, String value) {
        if (config.containsKey(filename)) {
            config.put(filename, new Properties());
        }

        Properties props = config.get(filename);

        props.put(key, value);

        if (propsRegistered) {
            try {
                platformSettingsService.saveBundleProperties(getSymbolicName(), filename, props);
            } catch (IOException e) {
                throw new MotechException("Can't save settings " + filename, e);
            }
        }
    }

    public void setProperty(String key, String value) {
        String filename = findFilename(key);

        if (filename == null) {
            throw new MotechException("No file containing key " + key);
        }

        setProperty(filename, key, value);
    }

    public void saveConfigProperties(String filename, Properties properties) {
        config.put(filename, properties);

        if (propsRegistered) {
            try {
                platformSettingsService.saveBundleProperties(getSymbolicName(), filename, properties);
            } catch (IOException e) {
                throw new RuntimeException("Can't save settings " + filename, e);
            }
        }
    }

    public void saveRawConfig(String filename, Resource resource) {
        rawConfig.put(filename, resource);

        try (InputStream is = resource.getInputStream()) {
            if (platformSettingsService != null) {
                platformSettingsService.saveRawConfig(getSymbolicName(), filename, is);
            }
        } catch (IOException e) {
            throw new MotechException("Error saving file " + filename, e);
        }
    }

    public void registerProperties(Resource resource) {
        String filename = getResourceFileName(resource);
        InputStream is = null;
        try {
            is = resource.getInputStream();

            Properties props =  new Properties();
            props.load(is);

            config.put(filename, props);
            registerProperties(filename, props);
        } catch (IOException e) {
            throw new MotechException("Error registering resource " + resource.getFilename(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public InputStream getRawConfig(String filename) {
        InputStream is = null;

        if (rawConfigRegistered) {
            // read from platform
            try {
                is = platformSettingsService.getRawConfig(getSymbolicName(), filename);
            } catch (IOException e) {
                throw new MotechException("Error loading file " + filename, e);
            }
        } else {
            // read resource
            Resource resource = rawConfig.get(filename);
            if (resource != null) {
                try {
                    is = resource.getInputStream();
                } catch (IOException e) {
                    throw new MotechException("Error reading raw config", e);
                }
            }
        }

        return is;
    }

    protected void registerAllProperties() {
        if (platformSettingsService != null) {
            for (Map.Entry<String, Properties> entry : config.entrySet()) {
                String filename = entry.getKey();
                Properties props = entry.getValue();

                registerProperties(filename, props);
            }
            propsRegistered = true;
        }
    }

    protected void registerProperties(String filename, Properties properties) {
        try {
            Properties registeredProps = platformSettingsService.getBundleProperties(getSymbolicName(), filename);

            if (registeredProps == null) {
                // register new props
                platformSettingsService.saveBundleProperties(getSymbolicName(), filename, properties);
            } else {
                // use registred props
                config.put(filename, registeredProps);
            }
        } catch (IOException e) {
            throw new MotechException("Cant register settings", e);
        }
    }

    protected void registerAllRawConfig() {
        if (platformSettingsService != null) {
            for (Map.Entry<String, Resource> entry : rawConfig.entrySet()) {
                String filename = entry.getKey();
                Resource resource = entry.getValue();

                if (!platformSettingsService.rawConfigExists(getSymbolicName(), filename)) {
                    // register new config with the platform
                    try (InputStream is = resource.getInputStream()) {
                        platformSettingsService.saveRawConfig(getSymbolicName(), filename, is);
                    } catch (IOException e) {
                        throw new MotechException("Can't save raw config " + filename, e);
                    }
                }
            }
            rawConfigRegistered = true;
        }
    }

    protected String getSymbolicName() {
        return bundleContext.getBundle().getSymbolicName();
    }

    protected String findFilename(String key) {
        String result = null;
        for (Map.Entry<String, Properties> entry : config.entrySet()) {
            Properties props = entry.getValue();
            String filename = entry.getKey();

            if (props.containsKey(key)) {
                result = filename;
                break;
            }
        }
        return result;
    }

    protected static String getResourceFileName(Resource resource) {
        String name = resource.getFilename();

        if (resource instanceof ClassPathResource) {
            name = ((ClassPathResource) resource).getPath();
        } else {
            int colonIndex = name.indexOf(':');
            if (colonIndex >= 0) {
                name = name.substring(colonIndex + 1);
            }
        }

        return name;
    }
}
