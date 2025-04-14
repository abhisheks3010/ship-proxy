package com.segmentcloud.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ShipProxyClient {
    private static final int LISTEN_PORT = 8080;
    private static String SERVER_HOST;
    private static int SERVER_PORT;

    public static void main(String[] args) throws Exception {
        SERVER_HOST = System.getenv("SERVER_HOST");
        SERVER_PORT = Integer.parseInt(System.getenv("SERVER_PORT"));

        Socket serverSocket = new Socket(SERVER_HOST, SERVER_PORT);
        System.out.println("Connected to Proxy Server...");

        ExecutorService executor = Executors.newSingleThreadExecutor();

        ServerSocket localProxy = new ServerSocket(LISTEN_PORT);
        System.out.println("Ship Proxy running on port " + LISTEN_PORT);

        while (true) {
            Socket browserSocket = localProxy.accept();
            executor.submit(() -> handleRequest(browserSocket, serverSocket));
        }
    }

    private static void handleRequest(Socket browserSocket, Socket serverSocket) {
        try {
            InputStream browserIn = browserSocket.getInputStream();
            OutputStream browserOut = browserSocket.getOutputStream();

            OutputStream serverOut = serverSocket.getOutputStream();
            InputStream serverIn = serverSocket.getInputStream();

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int n;
            while ((n = browserIn.read(data)) != -1) {
                buffer.write(data, 0, n);
                if (n < 4096) break;
            }

            byte[] requestData = buffer.toByteArray();
            synchronized (serverSocket) {
                serverOut.write(requestData);
                serverOut.flush();

                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                byte[] respData = new byte[4096];
                while ((n = serverIn.read(respData)) != -1) {
                    responseBuffer.write(respData, 0, n);
                    if (n < 4096) break;
                }

                byte[] response = responseBuffer.toByteArray();
                browserOut.write(response);
                browserOut.flush();
            }

            browserSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
