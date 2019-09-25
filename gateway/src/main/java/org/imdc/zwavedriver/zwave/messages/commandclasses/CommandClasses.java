package org.imdc.zwavedriver.zwave.messages.commandclasses;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat.ThermostatFanModeCommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat.ThermostatFanStateCommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat.ThermostatModeCommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat.ThermostatOperatingStateCommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat.ThermostatSetpointCommandClass;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;

import java.util.Comparator;

public enum CommandClasses {
    NOOP(0x00, null, "No Operation"),
    UNKNOWN(0xFF, null, "Unknown"),
    BASIC(0x20, new BasicCommandClass.Processor(), "Basic"),
    APPLICATION_STATUS(0x22, new ApplicationStatusCommandClass.Processor(), "Application Status"),
    SWITCH_BINARY(0x25, new SwitchBinaryCommandClass.Processor(), "Switch Binary"),
    MULTI_LEVEL_SWITCH(0x26, new MultiLevelSwitchCommandClass.Processor(), "Multi-Level Switch"),
    SENSOR_BINARY(0x30, new BinarySensorCommandClass.Processor(), "Sensor Binary"),
    MULTI_LEVEL_SENSOR(0x31, new MultiLevelSensorCommandClass.Processor(), "Multi-Level Sensor"),
    METER(0x32, new MeterCommandClass.Processor(), "Meter"),
    COLOR_SWITCH(0x33, new ColorSwitchCommandClass.Processor(), "Color Switch"),
    THERMOSTAT_MODE(0x40, new ThermostatModeCommandClass.Processor(), "Thermostat Mode"),
    THERMOSTAT_OPERATING_STATE(0x42, new ThermostatOperatingStateCommandClass.Processor(), "Thermostat Operating State"),
    THERMOSTAT_SETPOINT(0x43, new ThermostatSetpointCommandClass.Processor(), "Thermostat Setpoint"),
    THERMOSTAT_FAN_MODE(0x44, new ThermostatFanModeCommandClass.Processor(), "Thermostat Fan Mode"),
    THERMOSTAT_FAN_STATE(0x45, new ThermostatFanStateCommandClass.Processor(), "Thermostat Fan State"),
    DEVICE_RESET(0x5A, new DeviceResetCommandClass.Processor(), "Device Reset"),
    ZWAVE_PLUS_INFO(0x5E, new ZWavePlusInfoCommandClass.Processor(), "Z-Wave Plus Info"),
    MULTI_INSTANCE(0x60, new MultiInstanceCommandClass.Processor(), "Multi-Instance"),
    DOOR_LOCK(0x62, new DoorLockCommandClass.Processor(), "Door Lock"),
    USER_CODE(0x63, new UserCodeCommandClass.Processor(), "User Code"),
    BARRIER_OPERATOR(0x66, new BarrierOperatorCommandClass.Processor(), "Barrier Operator"),
    CONFIGURATION(0x70, new ConfigurationCommandClass.Processor(), "Configuration", 5),
    NOTIFICATION(0x71, new NotificationCommandClass.Processor(), "Notification"),
    MANUFACTURER_SPECIFIC(0x72, new ManufacturerSpecificCommandClass.Processor(), "Manufacturer Specific"),
    POWER_LEVEL(0x73, new PowerLevelCommandClass.Processor(), "Power Level"),
    PROTECTION(0x75, new ProtectionCommandClass.Processor(), "Protection"),
    BATTERY(0x80, new BatteryCommandClass.Processor(), "Battery"),
    CLOCK(0x81, new ClockCommandClass.Processor(), "Clock"),
    WAKEUP(0x84, new WakeUpCommandClass.Processor(), "Wakeup", 1),
    ASSOCIATION(0x85, new AssociationCommandClass.Processor(), "Association", 2),
    VERSION(0x86, new VersionCommandClass.Processor(), "Version", 3),
    SECURITY0(0x98, new Security0CommandClass.Processor(), "Security0", 4);

    private final byte commandClass;
    private final CommandProcessorAdapter processor;
    private final String name;
    private final int priority;

    CommandClasses(int commandClass, CommandProcessorAdapter processor, String name) {
        this(commandClass, processor, name, 100);
    }

    CommandClasses(int commandClass, CommandProcessorAdapter processor, String name, int priority) {
        this.commandClass = (byte) commandClass;
        this.processor = processor;
        this.name = name;
        this.priority = priority;
    }

    public byte getValue() {
        return commandClass;
    }

    public String getHex() {
        return Hex.asString(commandClass);
    }

    public String getName() {
        return name;
    }

    public int getPriority() {
        return priority;
    }

    public CommandProcessorAdapter getProcessor() {
        return processor;
    }

    public static CommandClasses from(int commandClass) throws DecoderException {
        for (CommandClasses c : CommandClasses.values()) {
            if (c.getValue() == (byte) commandClass && !c.equals(UNKNOWN)) {
                return c;
            }
        }
        throw new DecoderException("Unknown Command Class: " + Hex.asString(commandClass));
    }

    public static class CommandClassComparator implements Comparator<CommandClasses> {
        public int compare(CommandClasses c1, CommandClasses c2) {
            return Integer.compare(c1.getPriority(), c2.getPriority());
        }
    }

    @Override
    public String toString() {
        return String.format("{\"Command.Class\":{\"name\": %s, \"id\": %s}}", name(), Hex.asString(commandClass));
    }
}
