package com.jmeter.sampler.util;

import org.apache.jmeter.testelement.property.StringProperty;

public class DebugStringProperty extends StringProperty {
    public DebugStringProperty(String name, String value) {
        super(name, value);
        log.info("Created String property with name: " + name + ", value: " + value);
    }

    public DebugStringProperty() {
    }

    public void setObjectValue(Object v) {
        super.setObjectValue(v);
        log.info("Setting object value: " + v.toString());
    }

    public String getStringValue() {
        String value = super.getStringValue();
        log.info("getStringValue: " + value);
        return value;
    }

    public Object getObjectValue() {
        Object value = super.getObjectValue();
        log.info("getObjectValue: " + value.toString());
        return value;
    }

//    public Object clone() {
//        DebugStringProperty prop = (DebugStringProperty)super.clone();
//        return prop;
//    }

    public void setValue(String value) {
        super.setValue(value);
        log.info("SetValue: " + value);
    }
}
