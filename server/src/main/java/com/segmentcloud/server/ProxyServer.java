package com.segmentcloud.server;

import java.io.*;
import java.net.*;
import java.net.http.*;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ProxyServer {

    private static final int SERVER_PORT = 9000;

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        System.out.println("Proxy Server running on port " + SERVER_PORT);

        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("Ship Proxy connected");

            new Thread(() -> handleClient(clientSocket)).start();
        }
    }

    private static void handleClient(Socket clientSocket) {
        try (
                InputStream clientIn = clientSocket.getInputStream();
                OutputStream clientOut = clientSocket.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientIn, StandardCharsets.UTF_8))
        ) {
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.isEmpty()) return;

            String[] parts = firstLine.split(" ");
            if (parts.length < 2) return;

            String url = parts[1];

            // Consume all incoming headers from client
            String line;
            while ((line = reader.readLine()) != null && !line.isEmpty()) {
                // ignore client request headers
            }

            System.out.println("Fetching URL: " + url);

            HttpClient httpClient = HttpClient.newBuilder()
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept-Encoding", "identity")  // No decompression, get raw response
                    .build();

            HttpResponse<InputStream> response = httpClient.send(request, BodyHandlers.ofInputStream());

            // Send Status Line
            String statusLine = "HTTP/1.1 " + response.statusCode() + " OK\r\n";
            clientOut.write(statusLine.getBytes(StandardCharsets.UTF_8));

            // Forward All Headers exactly as received
            for (Map.Entry<String, List<String>> entry : response.headers().map().entrySet()) {
                for (String value : entry.getValue()) {
                    String headerLine = entry.getKey() + ": " + value + "\r\n";
                    clientOut.write(headerLine.getBytes(StandardCharsets.UTF_8));
                }
            }

            // End Headers
            clientOut.write("\r\n".getBytes(StandardCharsets.UTF_8));

            // Stream Body safely
            InputStream responseBody = response.body();
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = responseBody.read(buffer)) != -1) {
                clientOut.write(buffer, 0, bytesRead);
            }

            clientOut.flush();
            clientSocket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
