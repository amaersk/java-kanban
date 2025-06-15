package http.handler;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler {
    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_OK, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendCreated(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(HttpURLConnection.HTTP_CREATED, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    protected void sendNotFound(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND, 0);
        h.close();
    }

    protected void sendHasInteractions(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_NOT_ACCEPTABLE, 0);
        h.close();
    }

    protected void sendInternalServerError(HttpExchange h) throws IOException {
        h.sendResponseHeaders(HttpURLConnection.HTTP_INTERNAL_ERROR, 0);
        h.close();
    }
} 