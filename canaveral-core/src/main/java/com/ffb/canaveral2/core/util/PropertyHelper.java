package com.ffb.canaveral2.core.util;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import static com.google.common.base.Strings.isNullOrEmpty;

public class PropertyHelper {

    private static final Logger log = LoggerFactory.getLogger(PropertyHelper.class);

    public static void setProperties(Collection<String> propertyNames, String value) {
        Preconditions.checkNotNull(propertyNames, "Property names cannot be null");

        propertyNames.forEach(property -> setProperty(property, value));
    }

    public static void clearProperties(Set<Object> propertyNames) {
        Preconditions.checkNotNull(propertyNames, "Property names cannot be null");

        propertyNames.forEach(key -> System.clearProperty((String) key));
    }

    public static void setProperties(Collection<String> propertyNames, int value) {
        Preconditions.checkNotNull(propertyNames, "Property names cannot be null");

        if (!propertyNames.isEmpty()) {
            String sv = Integer.toString(value);
            propertyNames.forEach(property -> setProperty(property, sv));
        }
    }

    public static void setProperties(Properties systemProperties) {
        systemProperties.forEach((key, value) -> setProperty((String) key, (String) value));
    }

    public static void setProperty(String propertyName, String value) {
        if (!isNullOrEmpty(propertyName)) {
            String previous = System.setProperty(propertyName, value);
            if (previous != null) {
                log.info("Property '{}' previous value '{}' was overridden by '{}'.",
                        propertyName, previous, value);
            }
            log.info("Setting system property '{}' to '{}'.", propertyName, value);
        } else {
            log.debug("Empty property name was skipped.");
        }
    }
}
