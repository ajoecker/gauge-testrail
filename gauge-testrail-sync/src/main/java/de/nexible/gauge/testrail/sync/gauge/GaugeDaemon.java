// Copyright 2015 ThoughtWorks, Inc.

// This file is part of Gauge-Java.

// This program is free software.
//
// It is dual-licensed under:
// 1) the GNU General Public License as published by the Free Software Foundation,
// either version 3 of the License, or (at your option) any later version;
// or
// 2) the Eclipse Public License v1.0.
//
// You can redistribute it and/or modify it under the terms of either license.
// We would then provide copied of each license in a separate .txt file with the name of the license as the title of the file.

package de.nexible.gauge.testrail.sync.gauge;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.thoughtworks.gauge.Api;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.SynConfiguration;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

/**
 * Makes 2 important connections to the gauge core
 * <ul>
 * <li>Core gauge where messages are responded to based on message type.
 * <li>API gauge used to for asking gauge for common actions.
 * </ul>
 */
public class GaugeDaemon {
    private final SynConfiguration synConfiguration;
    private Socket gaugeSocket;

    public GaugeDaemon(SynConfiguration synConfiguration) {
        this.synConfiguration = synConfiguration;
    }

    public List<Spec.ProtoSpec> readSpecifications() {
        int daemonPort = synConfiguration.getDaemonPort();
        try {
            ProcessBuilder gaugeDaemon = new ProcessBuilder("gauge", "daemon", daemonPort + "");
            Path gaugeBackendDirectory = synConfiguration.getGaugeWorkingDirectory();
            gaugeDaemon.directory(gaugeBackendDirectory.toFile());
            Process start = gaugeDaemon.start();
            gaugeSocket = connect(daemonPort, synConfiguration.getDaemonServer());
            List<Spec.ProtoSpec> protoSpecList = fetchAllSteps();
            start.destroy();
            gaugeSocket.close();
            return protoSpecList;
        } catch (IOException e) {
            // it failed
        }
        return emptyList();
    }

    private static Socket connect(int port, String server) {
        Socket clientSocket;
        while (true) {
            try {
                clientSocket = new Socket(server, port);
                break;
            } catch (Exception ignored) {
            }
        }

        return clientSocket;
    }

    private List<Spec.ProtoSpec> fetchAllSteps() throws IOException {
        Api.APIMessage message = getStepRequest();
        Api.APIMessage response = getAPIResponse(message);
        Api.SpecsResponse specsResponse = response.getSpecsResponse();
        return specsResponse.getDetailsList().stream().map(Api.SpecsResponse.SpecDetail::getSpec).collect(Collectors.toList());
    }

    private Api.APIMessage getStepRequest() {
        Api.SpecsRequest spec = Api.SpecsRequest.newBuilder().build();
        return Api.APIMessage.newBuilder()
                .setMessageType(Api.APIMessage.APIMessageType.SpecsRequest)
                .setMessageId(2)
                .setSpecsRequest(spec)
                .build();
    }

    private Api.APIMessage getAPIResponse(Api.APIMessage message) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        CodedOutputStream cos = CodedOutputStream.newInstance(stream);
        byte[] bytes = message.toByteArray();
        cos.writeRawVarint64(bytes.length);
        cos.flush();
        stream.write(bytes);
        synchronized (gaugeSocket) {
            gaugeSocket.getOutputStream().write(stream.toByteArray());
            gaugeSocket.getOutputStream().flush();

            InputStream remoteStream = gaugeSocket.getInputStream();
            MessageLength messageLength = getMessageLength(remoteStream);
            bytes = toBytes(messageLength);
        }
        Api.APIMessage apiMessage = Api.APIMessage.parseFrom(bytes);
        return apiMessage;
    }

    private static byte[] toBytes(MessageLength messageLength) throws IOException {
        long messageSize = messageLength.getLength();
        CodedInputStream stream = messageLength.getRemainingStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        for (int i = 0; i < messageSize; i++) {
            outputStream.write(stream.readRawByte());
        }

        return outputStream.toByteArray();
    }


    private static MessageLength getMessageLength(InputStream is) throws IOException {
        CodedInputStream codedInputStream = CodedInputStream.newInstance(is);
        long size = codedInputStream.readRawVarint64();
        return new MessageLength(size, codedInputStream);
    }
}
