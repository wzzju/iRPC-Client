package cn.edu.ustc.irpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IPUClient {
    private static final Logger logger = Logger.getLogger(IPUClient.class.getName());
    private final ManagedChannel channel;
    private final IRPCGrpc.IRPCBlockingStub blockingStub;

    public IPUClient(String host, int port) {
        channel = ManagedChannelBuilder
                .forAddress(host, port)
                .usePlaintext(true)
                .build();
        blockingStub = IRPCGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void inference(String path) {
        logger.info("Will try to inference the file " + path + "...");

        InferenceData request = InferenceData
                .newBuilder()
                .setUserID(0)
                .setJobID(1)
                .setTaskName("googlenet.pb")
                .setDataDir("/tmp/images/")
                .setDataName("input.png")
                .build();
        InferenceResult response;
        try {
            response = blockingStub.inferenceProcess(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("The user id is " + response.getUserID() +
                " and the job id is " + response.getJobID() +
                ".\nThe inference result is shown below:\n" + response.getResult());
    }

    public static void main(String[] args) throws InterruptedException {
        String host = "192.168.0.79";//192.168.0.150
        if (args.length > 0) {
            host = args[0];
        }
        IPUClient ipuClient = new IPUClient(host, 8090);
        try {
            ipuClient.inference("E:" + File.separator + "images" + File.separator + "test.jpg");
        } finally {
            ipuClient.shutdown();
        }

    }
}
