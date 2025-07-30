package be.esmay.pocketidsync.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public abstract class BaseEndpoint {
    protected final PocketIDClient client;
    protected final Gson gson;

    protected BaseEndpoint(PocketIDClient client) {
        this.client = client;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(java.time.LocalDateTime.class, new LocalDateTimeAdapter())
                .setLenient()
                .create();
    }

    protected <T> CompletableFuture<T> get(String endpoint, Class<T> responseClass) {
        HttpRequest request = this.client.createRequest(endpoint)
                .GET()
                .build();

        return this.client.sendRequest(request).thenApply(response -> handleResponse(response, responseClass));
    }
    
    protected <T> CompletableFuture<T> getWithTypeToken(String endpoint, TypeToken<T> typeToken) {
        HttpRequest request = this.client.createRequest(endpoint)
                .GET()
                .build();

        return this.client.sendRequest(request).thenApply(response -> handleResponse(response, typeToken));
    }

    protected <T> CompletableFuture<T> post(String endpoint, Object body, Class<T> responseClass) {
        HttpRequest request = this.client.createRequest(endpoint)
                .POST(HttpRequest.BodyPublishers.ofString(this.gson.toJson(body)))
                .build();

        return this.client.sendRequest(request).thenApply(response -> handleResponse(response, responseClass));
    }

    protected <T> CompletableFuture<T> put(String endpoint, Object body, Class<T> responseClass) {
        HttpRequest request = this.client.createRequest(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(this.gson.toJson(body)))
                .build();

        return this.client.sendRequest(request).thenApply(response -> handleResponse(response, responseClass));
    }
    
    protected <T> CompletableFuture<T> putWithTypeToken(String endpoint, Object body, TypeToken<T> typeToken) {
        HttpRequest request = this.client.createRequest(endpoint)
                .PUT(HttpRequest.BodyPublishers.ofString(this.gson.toJson(body)))
                .build();

        return this.client.sendRequest(request).thenApply(response -> handleResponse(response, typeToken));
    }

    protected CompletableFuture<Void> delete(String endpoint) {
        HttpRequest request = this.client.createRequest(endpoint)
                .DELETE()
                .build();

        return this.client.sendRequest(request).thenApply(response -> {
            handleResponse(response, Void.class);
            return null;
        });
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            if (responseClass == Void.class)
                return null;

            return this.gson.fromJson(response.body(), responseClass);
        }

        throw new RuntimeException(new PocketIDException("API request failed", response.statusCode(), response.body()));
    }
    
    private <T> T handleResponse(HttpResponse<String> response, TypeToken<T> typeToken) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return this.gson.fromJson(response.body(), typeToken.getType());
            } catch (Exception e) {
                System.out.println("JSON parsing error: " + e.getMessage());
                System.out.println("Response body: " + response.body());
                throw new RuntimeException(new PocketIDException("JSON parsing failed: " + e.getMessage(), response.statusCode(), response.body()));
            }
        }

        System.out.println(response.body());

        throw new RuntimeException(new PocketIDException("API request failed", response.statusCode(), response.body()));
    }
}