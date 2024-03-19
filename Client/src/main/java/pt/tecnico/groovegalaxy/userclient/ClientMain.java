package pt.tecnico.groovegalaxy.userclient;
import pt.tecnico.groovegalaxy.userclient.grpc.ClientService;
import io.grpc.ManagedChannel;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;

import java.io.File;
import javax.net.ssl.SSLException;
import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;

public class ClientMain {

    public static void main(String[] args) throws SSLException {
        String serverHost = "localhost";
        int serverPort = 8080;
        
        if (args.length == 2) {
            serverHost = args[0];
            serverPort = Integer.parseInt(args[1]);
        }

        System.out.println("Setting up server connection on " + serverHost + ":" + serverPort);

        try {
            ManagedChannel channel = NettyChannelBuilder.forAddress(serverHost, serverPort)
                .sslContext(GrpcSslContexts.forClient().
                    trustManager(new File("certificates/ca.crt"))
                    .build())
                .overrideAuthority("server")
                .build();

            ClientService client = new ClientService(channel);
            client.parseInput();
            channel.shutdown();
        } catch (SSLException e) {
            System.err.println("SSLException: " + e.getMessage());
        }
    }

}
