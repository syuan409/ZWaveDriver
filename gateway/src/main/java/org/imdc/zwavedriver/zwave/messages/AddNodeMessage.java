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

package org.imdc.zwavedriver.zwave.messages;

import org.imdc.zwavedriver.zwave.Hex;
import org.imdc.zwavedriver.zwave.messages.commandclasses.Security0CommandClass;
import org.imdc.zwavedriver.zwave.messages.framework.AddStatus;
import org.imdc.zwavedriver.zwave.messages.framework.DecoderException;
import org.imdc.zwavedriver.zwave.messages.framework.InclusionMode;
import org.imdc.zwavedriver.zwave.messages.framework.Message;
import org.imdc.zwavedriver.zwave.messages.framework.MessageAdaptor;
import org.imdc.zwavedriver.zwave.messages.framework.MessageCommandProcessorAdapter;
import org.imdc.zwavedriver.zwave.messages.framework.MessageType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AddNodeMessage {

    private static final MessageType REQUEST = new MessageType(Messages.ADD_NODE, Message.Type.REQUEST);

    public static class Processor extends MessageCommandProcessorAdapter {
        @Override
        public Message process(MessageType messageType, byte[] message) throws DecoderException {
            return new Event(message);
        }
    }

    public static class Request extends MessageAdaptor {
        private InclusionMode inclusionMode;

        public Request(InclusionMode inclusionMode) {
            super(REQUEST);
            this.inclusionMode = inclusionMode;
        }

        @Override
        protected void addRequestData(ByteArrayOutputStream result) throws IOException {
            super.addRequestData(result);
            result.write(inclusionMode.value);
            result.write(0xFF); // ??
        }
    }

    public static class Event extends MessageAdaptor {
        public final AddStatus status;
        public final byte nodeId;

        public Event(byte[] message) throws DecoderException {
            super(message);
            in.read(); // ??
            status = AddStatus.from(in.read());
            nodeId = (byte) in.read();
        }

        @Override
        public void update() {
            if (status == AddStatus.PROTOCOL_DONE) {
                sendMessage(new Request(InclusionMode.STOP));
            } else if (status == AddStatus.DONE) {
                if (isSecureAdd()) {
                    sendCommand(nodeId, new Security0CommandClass.GetScheme(), false);
                    setSecureAdd(false);
                } else {
                    addNode(nodeId);
                }
            }
        }

        @Override
        public String toString() {
            return String.format("{\"AddNodeMessage.Event\": {\"status\": \"%s\", \"node\": %s}}", status.toString(), Hex.asString(nodeId));
        }
    }
}
