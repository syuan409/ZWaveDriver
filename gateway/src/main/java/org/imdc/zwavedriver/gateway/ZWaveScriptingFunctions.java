package org.imdc.zwavedriver.gateway;

import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.ClassUtils;

public class ZWaveScriptingFunctions extends ZWaveWrapperFunctions {
    private Class[] getClasses(String[] classesStr) throws Exception {
        Class[] classes = new Class[classesStr.length];
        for(int i = 0; i < classesStr.length; i++){
            String c = classesStr[i];
            Class cls = ClassUtils.getClass(c);
            classes[i] = cls;
        }
        return classes;
    }

    private Object[] getValues(Class[] classes, Object[] values) throws Exception {
        Object[] ret = new Object[values.length];
        for(int i = 0; i < values.length; i++){
            Class cls = classes[i];
            Object valIn = values[i];
            Object valOut = ConvertUtils.convert(valIn, cls);
            ret[i] = valOut;
        }
        return ret;
    }

    public void sendMessage(String className, String[] classesStr, Object[] args) throws Exception {
        Class<Message> c = (Class<Message>) Class.forName(className);
        Class[] classes = getClasses(classesStr);
        Message message = c.getDeclaredConstructor(classes).newInstance(getValues(classes, args));
        sendMessage(message);
    }

    public void sendCommand(int nodeId, boolean secure, String className, String[] classesStr, Object[] args) throws Exception {
        Class<CommandAdapter> c = (Class<CommandAdapter>) Class.forName(className);
        Class[] classes = getClasses(classesStr);
        CommandAdapter command = c.getDeclaredConstructor(classes).newInstance(getValues(classes, args));
        sendCommand(nodeId, command, secure);
    }
}
