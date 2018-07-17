package de.nexible.gauge.testrail.sync.gauge;

import com.google.protobuf.CodedInputStream;
import com.google.protobuf.CodedOutputStream;
import com.thoughtworks.gauge.Api;
import com.thoughtworks.gauge.Spec;
import de.nexible.gauge.testrail.sync.GaugeSpecRetrieval;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.thoughtworks.gauge.Api.APIMessage.newBuilder;
import static java.util.Collections.emptyList;

/**
 * The {@link GaugeSpecRetriever} fetches all specs from a Gauge report
 *
 * @author ajoecker
 */
public class GaugeSpecRetriever {
    private static final Logger logger = Logger.getLogger(GaugeSpecRetrieval.class.getName());

    /**
     * Fetches all specifications from the given socket
     *
     * @param gaugeSocket
     * @return
     * @throws IOException
     */
    public static List<Spec.ProtoSpec> fetchAllSpecs(Socket gaugeSocket) {
        try {
            logger.info(() -> "Retrieving all specs from Gauge");
            Api.APIMessage response = getAPIResponse(getSpecApiMessage(), gaugeSocket);
            Api.SpecsResponse specsResponse = response.getSpecsResponse();
            return specsResponse.getDetailsList().stream().map(Api.SpecsResponse.SpecDetail::getSpec).collect(Collectors.toList());
        } catch (IOException e) {
            // TODO logger
            return emptyList();
        }
    }

    private static Api.APIMessage getSpecApiMessage() {
        Api.SpecsRequest spec = Api.SpecsRequest.newBuilder().build();
        return newBuilder().setMessageType(Api.APIMessage.APIMessageType.SpecsRequest)
                .setMessageId(2)
                .setSpecsRequest(spec)
                .build();
    }

    private static Api.APIMessage getAPIResponse(Api.APIMessage message, Socket gaugeSocket) throws IOException {
        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            CodedOutputStream cos = CodedOutputStream.newInstance(stream);
            byte[] bytes = message.toByteArray();
            cos.writeRawVarint64(bytes.length);
            cos.flush();
            stream.write(bytes);
            synchronized (gaugeSocket) {
                gaugeSocket.getOutputStream().write(stream.toByteArray());
                gaugeSocket.getOutputStream().flush();

                InputStream remoteStream = gaugeSocket.getInputStream();
                bytes = toBytes(getMessageLength(remoteStream));
            }
            return Api.APIMessage.parseFrom(bytes);
        }
    }

    private static byte[] toBytes(MessageLength messageLength) throws IOException {
        long messageSize = messageLength.getLength();
        CodedInputStream stream = messageLength.getRemainingStream();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            for (int i = 0; i < messageSize; i++) {
                outputStream.write(stream.readRawByte());
            }

            return outputStream.toByteArray();
        }
    }


    private static MessageLength getMessageLength(InputStream is) throws IOException {
        CodedInputStream codedInputStream = CodedInputStream.newInstance(is);
        long size = codedInputStream.readRawVarint64();
        return new MessageLength(size, codedInputStream);
    }
}
