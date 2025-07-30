package be.esmay.pocketidsync.api;

import lombok.Getter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class PocketIDClient {

    @Getter
    private final HttpClient httpClient;
    private final String baseUrl;
    private final String apiKey;
    
    public PocketIDClient(String baseUrl, String apiKey) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }
    
    protected CompletableFuture<HttpResponse<String>> sendRequest(HttpRequest request) {
        return this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
    }
    
    protected HttpRequest.Builder createRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(this.baseUrl + "/api" + endpoint))
                .header("X-API-KEY", this.apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(30));
    }

}