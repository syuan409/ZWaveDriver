package org.imdc.zwavedriver.zwave.messages.commandclasses.framework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.CONSTRUCTOR)
@Retention(RetentionPolicy.RUNTIME)
public @interface CommandClassVersion {
    enum Version {
        UNKNOWN, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11
    }

    Version value();
}
