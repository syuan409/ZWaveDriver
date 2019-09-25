/**
 * Copyright (C) 2005-2015, Stefan Strömberg <stestr@nethome.nu>
 * <p>
 * This file is part of OpenNetHome.
 * <p>
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Report Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Report Public License for more details.
 * <p>
 * You should have received a copy of the GNU Report Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.imdc.zwavedriver.zwave.messages.commandclasses;


import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.ByteUtilities;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.MultipleReportsCommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Parameter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ParameterProperty;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.Size;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 * Configuration command class is used to read and write configuration parameters in nodes
 */
public class ConfigurationCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.CONFIGURATION, 0x04);
    private static final CommandCode GET = new CommandCode(CommandClasses.CONFIGURATION, 0x05);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.CONFIGURATION, 0x06);
    private static final CommandCode RESET = new CommandCode(CommandClasses.CONFIGURATION, 0x01);
    private static final CommandCode NAME_GET = new CommandCode(CommandClasses.CONFIGURATION, 0x0A);
    private static final CommandCode NAME_REPORT = new CommandCode(CommandClasses.CONFIGURATION, 0x0B);
    private static final CommandCode INFO_GET = new CommandCode(CommandClasses.CONFIGURATION, 0x0C);
    private static final CommandCode INFO_REPORT = new CommandCode(CommandClasses.CONFIGURATION, 0x0D);
    private static final CommandCode PROPERTIES_GET = new CommandCode(CommandClasses.CONFIGURATION, 0x0E);
    private static final CommandCode PROPERTIES_REPORT = new CommandCode(CommandClasses.CONFIGURATION, 0x0F);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{SET, GET, REPORT, RESET, NAME_GET, NAME_REPORT, INFO_GET, INFO_REPORT, PROPERTIES_GET, PROPERTIES_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(NAME_GET, NAME_REPORT).contains(commandCode)) {
                object = new ReportName(commandData);
            } else if (Arrays.asList(INFO_GET, INFO_REPORT).contains(commandCode)) {
                object = new ReportInfo(commandData);
            } else if (Arrays.asList(PROPERTIES_GET, PROPERTIES_REPORT).contains(commandCode)) {
                object = new ReportProperties(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Discover"), DataType.Boolean, false, true);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            if (path.get(0).equals("Discover")) {
                updateTag(path, false);
                for (int i = 0; i < 256; i++) {
                    sendCommand(path, new ConfigurationCommandClass.Get(i), secure);
                }
            } else if (path.get(0).equals("Configuration")) {
                String parameterNumberStr = path.get(1);
                int parameterNumber = Integer.parseInt(parameterNumberStr);

                if (path.get(2).equals("Refresh") && ((Boolean) o)) {
                    updateTag(path, false);
                    sendCommand(path, new ConfigurationCommandClass.Get(parameterNumber), secure);
                } else {
                    try {
                        Size size = Size.from((String) readTag(path.tag("Configuration", parameterNumberStr, "Size")));
                        sendCommand(path, new ConfigurationCommandClass.Set(new Parameter(parameterNumber, (Integer) o, size)), secure);
                    } catch (Exception ex) {
                        logger.error("Error writing to configuration tag", ex);
                        return QualityCode.Error;
                    }
                }
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        private final byte parameterNumber;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get(int parameterNumber) {
            super(GET);
            this.parameterNumber = (byte) parameterNumber;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(parameterNumber);
        }
    }

    public static class Set extends CommandAdapter {
        private final Parameter parameter;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(Parameter parameter) {
            super(SET);
            this.parameter = parameter;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            parameter.write(result);
        }
    }

    public static class Report extends CommandAdapter {
        public final Parameter parameter;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            parameter = new Parameter(in);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            String param = String.format("%d", parameter.parameterNumber);
            configureTagInitValue(path.tag("Configuration", param, "Refresh"), DataType.Boolean, 0, true);
            configureTag(path.tag("Configuration", param, "Name"), DataType.String, true);
            configureTagInitValue(path.tag("Configuration", param, "Size"), DataType.String, parameter.size.name);
            configureTagInitValue(path.tag("Configuration", param, "Value"), DataType.Int4, parameter.value, true);
        }

        @Override
        public String toString() {
            return String.format("{\"Configuration.Parameter.Report\": {\"parameter\": %s)", parameter.toString());
        }
    }

    public static class GetName extends CommandAdapter {
        private final int parameterNumber;

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetName(int parameterNumber) {
            this(NAME_GET, parameterNumber);
        }

        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetName(CommandCode commandCode, int parameterNumber) {
            super(commandCode);
            this.parameterNumber = parameterNumber;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            ByteUtilities.writeInt16MSB(result, parameterNumber);
        }
    }

    public static class ReportName extends MultipleReportsCommandAdapter {
        public int parameterNumber;
        public String name;

        public ReportName(byte[] commandData) throws DecoderException {
            super(commandData);
            processNextReport(commandData);
        }

        @Override
        public void processNextReport(byte[] commandData) {
            parameterNumber = ByteUtilities.readInt16MSB(in);
            reportsToFollow = in.read();

            String tmpName = ByteUtilities.readString(in, commandLength - 3);
            if (name == null) {
                name = tmpName;
            } else {
                name += tmpName;
            }
        }

        @Override
        public String toString() {
            return String.format("{\"Configuration.Parameter.Name.Report\": {\"parameter\": %d, \"name\": %s)", parameterNumber, name);
        }
    }

    public static class GetInfo extends GetName {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetInfo(int parameterNumber) {
            super(INFO_GET, parameterNumber);
        }
    }

    public static class ReportInfo extends ReportName {
        public ReportInfo(byte[] commandData) throws DecoderException {
            super(commandData);
        }

        @Override
        public String toString() {
            return String.format("{\"Configuration.Parameter.Info.Report\": {\"parameter\": %d, \"info\": %s)", parameterNumber, name);
        }
    }

    public static class GetProperties extends GetName {
        @CommandClassVersion(CommandClassVersion.Version.V3)
        public GetProperties(int parameterNumber) {
            super(PROPERTIES_GET, parameterNumber);
        }
    }

    public static class ReportProperties extends CommandAdapter {
        public final ParameterProperty property;

        public ReportProperties(byte[] commandData) throws DecoderException {
            super(commandData);
            property = new ParameterProperty(in);
        }

        @Override
        public String toString() {
            return String.format("{\"Configuration.Parameter.Properties.Report\": {\"property\": %s)", property.toString());
        }
    }

    public static class ResetDefault extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V4)
        public ResetDefault() {
            super(RESET);
        }
    }
}
