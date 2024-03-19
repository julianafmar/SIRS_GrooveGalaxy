package pt.tecnico.groovegalaxy.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyServerBuilder;


import java.sql.SQLException;
import java.util.Properties;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.*;
import java.security.cert.CertificateException;


import java.io.File;

public class ServerMain {
    static String user = "root";
    static String password = "";
    static boolean verbose = false;

    public static void main(String []args) throws IOException, ClassNotFoundException, InterruptedException, SQLException {
        int port = 8080;
        String dbHost = "localhost";
        int dbPort = 3306;
        String database = "SIRS";

        if (args.length == 4) {
            port = Integer.parseInt(args[0]);
            dbHost = args[1];
            dbPort = Integer.parseInt(args[2]);
            database = args[3];
        }
        else {
            port = Integer.parseInt(args[0]);
            dbHost = args[1];
            dbPort = Integer.parseInt(args[2]);
            database = args[3];
            for(int i = 0; i < args.length; i++) {
                if(args[i].equals("-v")) {
                    verbose = true;
                }
            }
        }
    
        String dbURL = "jdbc:mariadb://" + dbHost + ":" + dbPort + "/" + database;
        Class.forName("org.mariadb.jdbc.Driver");        

        // novo:
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);

        // Enable TLS/SSL
        properties.setProperty("useSSL", "true");
        properties.setProperty("requireSSL", "true");

        properties.setProperty("verifyServerCertificate", "true");

        properties.setProperty("clientCertificateKeyStoreUrl", new File("certificates_database/serverAPI.p12").toURI().toURL().toString());
        properties.setProperty("clientCertificateKeyStorePassword", "changeme");
        properties.setProperty("serverSslCert", "certificates_database/database-cert.pem");
        Connection dbConnection = DriverManager.getConnection(dbURL, properties);


        Server server = NettyServerBuilder.forPort(port)
            .useTransportSecurity(new File("certificates_client/server.crt"), new File("certificates_client/server.key")) 
            .addService(new ServerImpl(dbConnection, verbose))
            .build();

        server.start();
        System.out.println("Server started");
        server.awaitTermination();
        dbConnection.close();
    }
}
