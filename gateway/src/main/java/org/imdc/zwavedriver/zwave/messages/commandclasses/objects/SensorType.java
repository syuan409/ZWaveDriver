package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

public enum SensorType {
    TEMPERATURE(0x01, 0, 0x01, "Temperature"),
    GENERAL_PURPOSE(0x02, 0, 0x02, "General"),
    LUMINANCE(0x03, 0, 0x04, "Luminance"),
    POWER(0x04, 0, 0x08, "Power"),
    HUMIDITY(0x05, 0, 0x10, "Humidity"),
    VELOCITY(0x06, 0, 0x20, "Velocity"),
    DIRECTION(0x07, 0, 0x40, "Direction"),
    ATMOSPHERIC_PRESSURE(0x08, 0, 0x80, "Atmospheric Pressure"),
    BAROMETRIC_PRESSURE(0x09, 1, 0x01, "Barometric Pressure"),
    SOLAR_RADIATION(0x0A, 1, 0x02, "Solar Radiation"),
    DEW_POINT(0x0B, 1, 0x04, "Dew Point"),
    RAIN_RATE(0x0C, 1, 0x08, "Rain Rate"),
    TIDE_LEVEL(0x0D, 1, 0x10, "Tide Level"),
    WEIGHT(0x0E, 1, 0x20, "Weight"),
    VOLTAGE(0x0F, 1, 0x40, "Voltage"),
    CURRENT(0x10, 1, 0x80, "Current"),
    CO2_LEVEL(0x11, 2, 0x01, "CO2 Level"),
    AIR_FLOW(0x12, 2, 0x02, "Air Flow"),
    TANK_CAPACITY(0x13, 2, 0x04, "Tank Capacity"),
    DISTANCE(0x14, 2, 0x08, "Distance"),
    ANGLE_POSITION(0x15, 2, 0x10, "Angle Position"),
    ROTATION(0x16, 2, 0x20, "Rotation"),
    WATER_TEMPERATURE(0x17, 2, 0x40, "Water Temperature"),
    SOIL_TEMPERATURE(0x18, 2, 0x80, "Soil Temperature"),
    SEISMIC_INTENSITY(0x19, 3, 0x01, "Seismic Intensity"),
    SEISMIC_MAGNITUDE(0x1A, 3, 0x02, "Seismic Magnitude"),
    ULTRAVIOLET(0x1B, 3, 0x04, "Ultraviolet"),
    ELECTRICAL_RESISTIVITY(0x1C, 3, 0x08, "Electrical Resistivity"),
    ELECTRICAL_CONDUCTIVITY(0x1D, 3, 0x10, "Electrical Conductivity"),
    LOUDNESS(0x1E, 3, 0x20, "Loudness"),
    MOISTURE(0x1F, 3, 0x40, "Moisture"),
    FREQUENCY(0x20, 3, 0x80, "Frequency"),
    TIME(0x21, 4, 0x01, "Time"),
    TARGET_TEMPERATURE(0x22, 4, 0x02, "Target Temperature"),
    PARTICULATE_MATTER_2_5(0x23, 4, 0x04, "Particulate Matter 2.5"),
    FORMALDEHYDE_LEVEL(0x24, 4, 0x08, "Formaldehyde Level"),
    RADON_CONCENTRATION(0x25, 4, 0x10, "Radon Concentration"),
    METHANE_DENSITY(0x26, 4, 0x20, "Methan Density"),
    VOLATILE_ORGANIC_COMPOUND_LEVEL(0x27, 4, 0x40, "Volatile Organic Compound Level"),
    CARBON_MONOXIDE_LEVEL(0x28, 4, 0x80, "Carbon Monoxide Level"),
    SOIL_HUMIDITY(0x29, 5, 0x01, "Soil Humidity"),
    SOIL_REACTIVITY(0x2A, 5, 0x02, "Soil Reactivity"),
    SOIL_SALINITY(0x2B, 5, 0x04, "Soil Salinity"),
    HEART_RATE(0x2C, 5, 0x08, "Hear Rate"),
    BLOOD_PRESSURE(0x2D, 5, 0x10, "Blood Pressure"),
    MUSCLE_MASS(0x2E, 5, 0x20, "Muscle Mass"),
    FAT_MASS(0x2F, 5, 0x40, "Fat Mass"),
    BONE_MASS(0x30, 5, 0x80, "Bone Mass"),
    TOTAL_BODY_WATER(0x31, 6, 0x01, "Total Body Water"),
    BASIS_METABOLIC_RATE(0x32, 6, 0x02, "Basis Metabolic Rate"),
    BODY_MASS_INDEX(0x33, 6, 0x04, "Body Mass Index"),
    ACCELERATION_X_AXIS(0x34, 6, 0x08, "Acceleration X Axis"),
    ACCELERATION_Y_AXIS(0x35, 6, 0x10, "Acceleration Y Axis"),
    ACCELERATION_Z_AXIS(0x36, 6, 0x20, "Acceleration Z Axis"),
    SMOKE_DENSITY(0x37, 6, 0x40, "Smoke Density"),
    WATER_FLOW(0x38, 6, 0x80, "Water Flow"),
    WATER_PRESSURE(0x39, 7, 0x01, "Water Pressure"),
    RF_SIGNAL_STRENGTH(0x3A, 7, 0x02, "RF Signal Strength"),
    PARTICULATE_MATTER_10(0x3B, 7, 0x04, "Particulate Matter 10"),
    RESPIRATORY_RATE(0x3C, 7, 0x08, "Respiratory Rate"),
    RELATIVE_MODULATION_LEVEL(0x3D, 7, 0x10, "Relative Modulation Level"),
    BOILER_WATER_TEMPERATURE(0x3E, 7, 0x20, "Boiler Water Temperature"),
    DOMESTIC_HOT_WATER_TEMPERATURE(0x3F, 7, 0x40, "Domestic Hot Water Temperature"),
    OUTSIDE_TEMPERATURE(0x40, 7, 0x80, "Outside Temperature"),
    EXHAUST_TEMPERATURE(0x41, 8, 0x01, "Exhaust Temperature"),
    WATER_CHLORINE_LEVEL(0x42, 8, 0x02, "Water Chlorine Level"),
    WATER_ACIDITY(0x43, 8, 0x04, "Water Acidity"),
    WATER_OXIDATION(0x44, 8, 0x08, "Water Oxidation");

    final public byte value;
    final public byte supportedByte;
    final public byte supportedBitMask;
    final public String name;

    SensorType(int value, int supportedByte, int supportedBitMask, String name) {
        this.value = (byte) value;
        this.supportedByte = (byte) supportedByte;
        this.supportedBitMask = (byte) supportedBitMask;
        this.name = name;
    }

    public static SensorType from(int type) throws DecoderException {
        for (SensorType t : values()) {
            if (t.value == (byte) type) {
                return t;
            }
        }
        throw new DecoderException("Unknown Sensor Type");
    }

    public static Dataset buildDS() {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Value", "SupportedByte", "SupportedBitMask");
        builder.colTypes(String.class, Byte.class, Byte.class, Byte.class);
        for (SensorType r : values()) {
            builder.addRow(r.name, r.value, r.supportedByte, r.supportedBitMask);
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Sensor.Type\":{\"name\": %s}}", name);
    }
}
