package com.example.todomanager;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

public class ConsoleUserClient {

    private static final String BASE = "http://localhost:8080/api/users";
    private final HttpClient client = HttpClient.newHttpClient();

    public static void main(String[] args) {
        ConsoleUserClient tester = new ConsoleUserClient();
        tester.runTests();
    }

    public void runTests() {
        try {
            System.out.println("==== USER CONTROLLER TEST ====");

            String username = "cli_user_" + UUID.randomUUID().toString().substring(0, 5);
            String password = "pass123";

            // 1️⃣ CREATE USER
            String createJson = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";
            String createResponse = sendPost(BASE, createJson);
            System.out.println("CREATE RESPONSE: " + createResponse);

            String id = extractId(createResponse);
            if (id == null) {
                System.out.println("Could not extract user id. Aborting test.");
                return;
            }

            // 2️⃣ GET USER
            String getResponse = sendGet(BASE + "/" + id);
            System.out.println("GET RESPONSE: " + getResponse);

            // 3️⃣ LOGIN
            String loginJson = "{ \"username\": \"" + username + "\", \"password\": \"" + password + "\" }";
            String loginResponse = sendPost(BASE + "/login", loginJson);
            System.out.println("LOGIN RESPONSE: " + loginResponse);

            // 4️⃣ UPDATE USER
            String updateJson = "{ \"username\": \"" + username + "_updated\", \"password\": \"newpass\" }";
            String updateResponse = sendPut(BASE + "/" + id, updateJson);
            System.out.println("UPDATE RESPONSE: " + updateResponse);

            // 5️⃣ DELETE USER
            String deleteResponse = sendDelete(BASE + "/" + id);
            System.out.println("DELETE RESPONSE: " + deleteResponse);

            System.out.println("==== TEST COMPLETE ====");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private String sendPost(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private String sendPut(String url, String json) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    private String sendDelete(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .DELETE()
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        return response.body();
    }

    /**
     * Very simple extraction of "id":"<uuid>" from JSON response.
     * Assumes your controller returns something like:
     * { "id":"...", "username":"...", "password":"..." }
     */
    private String extractId(String json) {
        try {
            int start = json.indexOf("\"id\"");
            if (start == -1) return null;

            int colon = json.indexOf(":", start);
            int firstQuote = json.indexOf("\"", colon);
            int secondQuote = json.indexOf("\"", firstQuote + 1);

            return json.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            return null;
        }
    }
}