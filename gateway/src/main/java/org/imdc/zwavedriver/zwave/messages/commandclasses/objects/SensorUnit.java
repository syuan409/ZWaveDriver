package org.imdc.zwavedriver.zwave.messages.commandclasses.objects;

import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.util.DatasetBuilder;

import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ACCELERATION_X_AXIS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ACCELERATION_Y_AXIS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ACCELERATION_Z_AXIS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.AIR_FLOW;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ANGLE_POSITION;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ATMOSPHERIC_PRESSURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.BAROMETRIC_PRESSURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.BASIS_METABOLIC_RATE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.BLOOD_PRESSURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.BODY_MASS_INDEX;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.BOILER_WATER_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.BONE_MASS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.CARBON_MONOXIDE_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.CO2_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.CURRENT;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.DEW_POINT;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.DISTANCE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.DOMESTIC_HOT_WATER_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ELECTRICAL_CONDUCTIVITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ELECTRICAL_RESISTIVITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.EXHAUST_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.FAT_MASS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.FORMALDEHYDE_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.GENERAL_PURPOSE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.HEART_RATE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.HUMIDITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.LOUDNESS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.LUMINANCE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.METHANE_DENSITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.MOISTURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.MUSCLE_MASS;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.OUTSIDE_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.PARTICULATE_MATTER_10;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.PARTICULATE_MATTER_2_5;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.POWER;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.RAIN_RATE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.RELATIVE_MODULATION_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.RESPIRATORY_RATE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.RF_SIGNAL_STRENGTH;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ROTATION;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SEISMIC_INTENSITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SEISMIC_MAGNITUDE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SMOKE_DENSITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SOIL_HUMIDITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SOIL_REACTIVITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SOIL_SALINITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SOIL_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.SOLAR_RADIATION;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.TANK_CAPACITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.TARGET_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.TIDE_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.TIME;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.TOTAL_BODY_WATER;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.ULTRAVIOLET;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.VELOCITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.VOLATILE_ORGANIC_COMPOUND_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.VOLTAGE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WATER_ACIDITY;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WATER_CHLORINE_LEVEL;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WATER_FLOW;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WATER_OXIDATION;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WATER_PRESSURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WATER_TEMPERATURE;
import static org.imdc.zwavedriver.zwave.messages.commandclasses.objects.SensorType.WEIGHT;

public enum SensorUnit {
    C(new SensorType[]{TEMPERATURE, DEW_POINT, WATER_TEMPERATURE, SOIL_TEMPERATURE, TARGET_TEMPERATURE, BOILER_WATER_TEMPERATURE, DOMESTIC_HOT_WATER_TEMPERATURE, OUTSIDE_TEMPERATURE, EXHAUST_TEMPERATURE}, 0x00, 0x01, "C"),
    F(new SensorType[]{TEMPERATURE, DEW_POINT, WATER_TEMPERATURE, SOIL_TEMPERATURE, TARGET_TEMPERATURE}, 0x01, 0x02, "F"),
    DEGREES1(ANGLE_POSITION, 0x01, 0x02, "deg"),
    DEGREES2(ANGLE_POSITION, 0x02, 0x04, "deg"),
    PERCENTAGE(new SensorType[]{GENERAL_PURPOSE, LUMINANCE, HUMIDITY, ANGLE_POSITION, MOISTURE, SOIL_HUMIDITY, SMOKE_DENSITY, RELATIVE_MODULATION_LEVEL}, 0x00, 0x01, "%"),
    DIMENSIONLESS(GENERAL_PURPOSE, 0x01, 0x02, ""),
    LUX(LUMINANCE, 0x01, 0x02, "Lux"),
    WATT(POWER, 0x00, 0x01, "W"),
    BTU_H(POWER, 0x01, 0x02, "Btu/h"),
    ABSOLUTE_HUMIDITY(HUMIDITY, 0x01, 0x02, "g/m3"),
    M_S(VELOCITY, 0x00, 0x01, "m/s"),
    MPH(VELOCITY, 0x01, 0x02, "mph"),
    DIRECTION(SensorType.DIRECTION, 0x00, 0x01, ""),
    KPA(new SensorType[]{ATMOSPHERIC_PRESSURE, BAROMETRIC_PRESSURE, WATER_PRESSURE}, 0x00, 0x01, "kPa"),
    IOM(new SensorType[]{ATMOSPHERIC_PRESSURE, BAROMETRIC_PRESSURE}, 0x01, 0x02, "ioM"),
    W_M2(SOLAR_RADIATION, 0x00, 0x01, "W/m2"),
    MM_H(RAIN_RATE, 0x00, 0x01, "mm/h"),
    I_H(RAIN_RATE, 0x01, 0x02, "in/h"),
    M(new SensorType[]{TIDE_LEVEL, DISTANCE}, 0x00, 0x01, "m"),
    FT(new SensorType[]{TIDE_LEVEL}, 0x01, 0x02, "ft"),
    CM(DISTANCE, 0x01, 0x02, "cm"),
    DFT(DISTANCE, 0x02, 0x04, "ft"),
    KG(new SensorType[]{WEIGHT, MUSCLE_MASS, FAT_MASS, BONE_MASS, TOTAL_BODY_WATER}, 0x00, 0x01, "kg"),
    LB(WEIGHT, 0x01, 0x02, "lb"),
    V(VOLTAGE, 0x00, 0x01, "V"),
    MV(VOLTAGE, 0x01, 0x02, "mV"),
    A(CURRENT, 0x00, 0x01, "A"),
    MA(CURRENT, 0x01, 0x02, "mA"),
    PPM(CO2_LEVEL, 0x00, 0x01, "ppm"),
    M3_H(AIR_FLOW, 0x00, 0x01, "m3/h"),
    CFM(AIR_FLOW, 0x01, 0x02, "cfm"),
    L(TANK_CAPACITY, 0x00, 0x01, "l"),
    C_M(TANK_CAPACITY, 0x01, 0x02, "c3"),
    GAL(TANK_CAPACITY, 0x02, 0x04, "gal"),
    RPM(ROTATION, 0x00, 0x01, "rpm"),
    HZ(ROTATION, 0x01, 0x02, "Hz"),
    MERCALLI(SEISMIC_INTENSITY, 0x00, 0x01, "Mercalli"),
    EUROPEAN_MACROSEISMIC(SEISMIC_INTENSITY, 0x01, 0x02, "European Macroseismic"),
    LIEDU(SEISMIC_INTENSITY, 0x02, 0x04, "Liedu"),
    SHINDO(SEISMIC_INTENSITY, 0x03, 0x08, "Shindo"),
    LOCAL(SEISMIC_MAGNITUDE, 0x00, 0x01, "Local"),
    MOMENT(SEISMIC_MAGNITUDE, 0x01, 0x02, "Moment"),
    SURFACE_WAVE(SEISMIC_MAGNITUDE, 0x02, 0x04, "Surface Wave"),
    BODY_WAVE(SEISMIC_MAGNITUDE, 0x03, 0x08, "Body Wave"),
    UV(ULTRAVIOLET, 0x00, 0x01, "UV"),
    OHM_METER(ELECTRICAL_RESISTIVITY, 0x00, 0x01, "ohm"),
    SIEMENS_METER(ELECTRICAL_CONDUCTIVITY, 0x00, 0x01, "S/m"),
    DECIBEL(LOUDNESS, 0x00, 0x01, "dB"),
    A_WEIGHTED_DECIBEL(LOUDNESS, 0x01, 0x02, "A dB"),
    VOLUME_WATER(MOISTURE, 0x01, 0x02, "m3/m3"),
    IMPEDANCE(MOISTURE, 0x02, 0x04, "kohm"),
    WATER_ACTIVITY(MOISTURE, 0x03, 0x08, "aw"),
    FREQUENCY_HERTZ(ROTATION, 0x00, 0x01, "Hz"),
    KHZ(ROTATION, 0x01, 0x02, "kHz"),
    TIME_SEC(TIME, 0x00, 0x01, "s"),
    MOLE_C_M(new SensorType[]{PARTICULATE_MATTER_2_5, FORMALDEHYDE_LEVEL, METHANE_DENSITY, VOLATILE_ORGANIC_COMPOUND_LEVEL, CARBON_MONOXIDE_LEVEL, SOIL_SALINITY, PARTICULATE_MATTER_10}, 0x00, 0x01, "mol/m3"),
    MG_C_M(new SensorType[]{PARTICULATE_MATTER_2_5, PARTICULATE_MATTER_10}, 0x01, 0x02, "μg/m3"),
    B_C_M(FORMALDEHYDE_LEVEL, 0x00, 0x01, "bq/m3"),
    P_L(FORMALDEHYDE_LEVEL, 0x01, 0x02, "pCi/l"),
    O_PPM(new SensorType[]{VOLATILE_ORGANIC_COMPOUND_LEVEL, CARBON_MONOXIDE_LEVEL}, 0x01, 0x02, "ppm"),
    PH(new SensorType[]{SOIL_REACTIVITY, WATER_ACIDITY}, 0x00, 0x01, "pH"),
    BPM(new SensorType[]{HEART_RATE, RESPIRATORY_RATE}, 0x00, 0x01, "bpm"),
    SYSTOLIC(BLOOD_PRESSURE, 0x00, 0x01, "mmHg"),
    DIASTOLIC(BLOOD_PRESSURE, 0x01, 0x02, "mmHg"),
    J(BASIS_METABOLIC_RATE, 0x00, 0x01, "J"),
    BMI(BODY_MASS_INDEX, 0x00, 0x01, "BMI"),
    M_S_S(new SensorType[]{ACCELERATION_X_AXIS, ACCELERATION_Y_AXIS, ACCELERATION_Z_AXIS}, 0x00, 0x01, "m/s2"),
    L_H(WATER_FLOW, 0x00, 0x01, "l/h"),
    RSSI(RF_SIGNAL_STRENGTH, 0x00, 0x01, "RSSI"),
    DBM(RF_SIGNAL_STRENGTH, 0x01, 0x02, "dBm"),
    MG_L(WATER_CHLORINE_LEVEL, 0x00, 0x01, "mg/l"),
    W_MV(WATER_OXIDATION, 0x00, 0x01, "mV");

    final public SensorType[] types;
    final public byte scale;
    final public byte supportedBitMask;
    final public String unit;

    SensorUnit(SensorType type, int scale, int supportedBitMask, String unit) {
        this(new SensorType[]{type}, scale, supportedBitMask, unit);
    }

    SensorUnit(SensorType[] types, int scale, int supportedBitMask, String unit) {
        this.types = types;
        this.scale = (byte) scale;
        this.supportedBitMask = (byte) supportedBitMask;
        this.unit = unit;
    }

    public static SensorUnit fromMeterScale(SensorType sensorType, String scale) throws DecoderException {
        for (SensorUnit u : values()) {
            for (SensorType t : u.types) {
                if ((t == sensorType) && (u.unit.equals(scale))) {
                    return u;
                }
            }
        }
        throw new DecoderException("Unknown Sensor Unit");
    }

    public static SensorUnit fromMeterScale(SensorType sensorType, int scale) throws DecoderException {
        for (SensorUnit u : values()) {
            for (SensorType t : u.types) {
                if ((t == sensorType) && (u.scale == (byte) scale)) {
                    return u;
                }
            }
        }
        throw new DecoderException("Unknown Sensor Unit");
    }

    public static Dataset buildDS() {
        return buildDS(null);
    }

    public static Dataset buildDS(SensorType sensorType) {
        DatasetBuilder builder = new DatasetBuilder();
        builder.colNames("Name", "Type", "Scale", "SupportedBitMask");
        builder.colTypes(String.class, Byte.class, Byte.class, Byte.class);
        for (SensorUnit u : values()) {
            for (SensorType t : u.types) {
                if (sensorType == null || sensorType == t) {
                    builder.addRow(u.unit, t.name, u.scale, u.supportedBitMask);
                }
            }
        }
        return builder.build();
    }

    @Override
    public String toString() {
        return String.format("{\"Sensor.Unit\":{\"name\": %s, \"unit\": %s}}", name(), unit);
    }
}
