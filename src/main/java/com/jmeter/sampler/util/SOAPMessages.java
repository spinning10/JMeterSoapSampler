package com.jmeter.sampler.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.apache.jmeter.util.JMeterUtils;

public class SOAPMessages {
    private static ResourceBundle resources = null;
    private static Locale locale = null;
    public static final String RES_KEY_PFX = "[res_key=";

    public SOAPMessages() {
    }

    private static void initResources() {
        if(resources == null || locale != JMeterUtils.getLocale()) {
            locale = JMeterUtils.getLocale();
            resources = ResourceBundle.getBundle("soapmessages", locale);
        }

    }

    public static String getResString(String key) {
        return getResStringDefault(key, "[res_key=" + key + "]");
    }

    private static String getResStringDefault(String key, String defaultValue) {
        if(key == null) {
            return null;
        } else {
            initResources();
            key = key.replace(' ', '_');
            key = key.toLowerCase(Locale.ENGLISH);
            String resString = null;

            try {
                resString = resources.getString(key);
            } catch (MissingResourceException var4) {
                resString = defaultValue;
            }

            return resString;
        }
    }
}
