package de.nexible.gauge.testrail.sync.gauge;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.thoughtworks.gauge.Api;
import com.thoughtworks.gauge.Spec;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;

public class GaugeStepRetriever {
    public List<Spec.ProtoSpec> fetchAllSteps(Socket gaugeSocket) throws IOException {
        Api.APIMessage message = getStepRequest();
        Api.APIMessage response = getAPIResponse(message, gaugeSocket);
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

    private Api.APIMessage getAPIResponse(Api.APIMessage message, Socket gaugeSocket) throws IOException {
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
