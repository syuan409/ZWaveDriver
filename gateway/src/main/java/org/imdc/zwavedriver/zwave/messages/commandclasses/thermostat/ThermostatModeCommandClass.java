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

package org.imdc.zwavedriver.zwave.messages.commandclasses.thermostat;


import org.imdc.zwavedriver.gateway.ZWavePath;
import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.CommandClasses;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.Command;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandArgument;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClass;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandClassVersion;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandCode;
import org.imdc.zwavedriver.zwave.messages.commandclasses.framework.CommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.commandclasses.objects.ThermostatMode;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import com.inductiveautomation.ignition.common.model.values.QualityCode;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class ThermostatModeCommandClass implements CommandClass {

    private static final CommandCode SET = new CommandCode(CommandClasses.THERMOSTAT_MODE, 0x01);
    private static final CommandCode GET = new CommandCode(CommandClasses.THERMOSTAT_MODE, 0x02);
    private static final CommandCode REPORT = new CommandCode(CommandClasses.THERMOSTAT_MODE, 0x03);
    private static final CommandCode SUPPORTED_GET = new CommandCode(CommandClasses.THERMOSTAT_MODE, 0x04);
    private static final CommandCode SUPPORTED_REPORT = new CommandCode(CommandClasses.THERMOSTAT_MODE, 0x05);

    public static class Processor extends CommandProcessorAdapter {
        @Override
        public CommandCode[] getCommandCodes() {
            return new CommandCode[]{GET, SET, REPORT, SUPPORTED_GET, SUPPORTED_REPORT};
        }

        @Override
        public Command processCommandData(CommandCode commandCode, CommandArgument argument, byte[] commandData) throws DecoderException {
            Command object = null;
            if (Arrays.asList(GET, SET, REPORT).contains(commandCode)) {
                object = new Report(commandData);
            } else if (Arrays.asList(SUPPORTED_GET, SUPPORTED_REPORT).contains(commandCode)) {
                object = new ReportSupported(commandData);
            }
            return object;
        }

        @Override
        public void configureTags(ZWavePath path, int version) {
            configureTagInitValue(path.tag("Lookup", "Modes"), DataType.DataSet, ThermostatMode.buildDS());
            configureTag(path.tag("Mode"), DataType.String, true);
        }

        @Override
        public void queueInitialMessages(ZWavePath path, int version, boolean secure, boolean initial) {
            sendCommand(path, new ThermostatModeCommandClass.Get(), secure);
        }

        @Override
        public QualityCode write(ZWavePath path, int version, boolean secure, Object o) {
            try {
                ThermostatMode mode = ThermostatMode.from((String) o);
                sendCommand(path, new ThermostatModeCommandClass.Set(mode), secure);
            } catch (Exception ex) {
                logger.error("Error writing to thermostat mode tag", ex);
                return QualityCode.Error;
            }
            return QualityCode.Good;
        }
    }

    public static class Get extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Get() {
            super(GET);
        }
    }

    public static class Set extends CommandAdapter {
        private final ThermostatMode mode;

        @CommandClassVersion(CommandClassVersion.Version.V1)
        public Set(ThermostatMode mode) {
            super(SET);
            this.mode = mode;
        }

        @Override
        protected void addCommandData(ByteArrayOutputStream result) {
            super.addCommandData(result);
            result.write(mode.value);
        }
    }

    private static class Report extends CommandAdapter {
        public final ThermostatMode mode;

        public Report(byte[] commandData) throws DecoderException {
            super(commandData);
            int b = in.read();
            mode = ThermostatMode.from(b & 0x1F);
        }

        @Override
        public void update(ZWavePath path, int version, boolean secure) {
            updateTag(path.tag("Mode"), mode.name);
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatMode.Report\":{\"mode\": %s}}", mode.toString());
        }
    }

    public static class GetSupported extends CommandAdapter {
        @CommandClassVersion(CommandClassVersion.Version.V1)
        public GetSupported() {
            super(SUPPORTED_GET);
        }
    }

    private static class ReportSupported extends CommandAdapter {
        public final List<ThermostatMode> supportedModes;

        public ReportSupported(byte[] commandData) throws DecoderException {
            super(commandData);
            supportedModes = new ArrayList();
            byte[] masks = getCommandData(commandData);
            for (int i = 0; i < masks.length; i++) {
                for (int b = 0; b < 8; b++) {
                    if (((masks[i] >> b) & 0x01) != 0) {
                        supportedModes.add(ThermostatMode.from((i * 8) + b));
                    }
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"ThermostatMode.Supported.Report\":{\"modes\": [%s]}}", Hex.asString(supportedModes));
        }
    }
}