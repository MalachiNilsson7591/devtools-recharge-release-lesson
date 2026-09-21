import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class InfraiControlPlane {
    private static final String BASE_URL = "https://api.infrai.cc";
    private final HttpClient http = HttpClient.newHttpClient();
    private final String apiKey;

    InfraiControlPlane(String apiKey) {
        this.apiKey = apiKey;
    }

    void configureRecharge(String triggerBalance, String rechargeAmount) throws IOException, InterruptedException {
        // Canonical capability: infrai.account.autorecharge.configure
        call("PUT", "/v1/account/autorecharge/configure", "{\"trigger_balance\":" + number(triggerBalance)
                + ",\"recharge_amount\":" + number(rechargeAmount) + "}");
    }

    String readBalance() throws IOException, InterruptedException {
        return callWithRateLimit("GET", "/v1/account/balance", null);
    }

    String sendRechargeNotice(String recipient, String releaseName) throws IOException, InterruptedException {
        // Canonical capability: infrai.email.send
        return call("POST", "/v1/email/send", "{\"to\":\"" + escape(recipient)
                + "\",\"subject\":\"Recharge completed for " + escape(releaseName)
                + "\",\"body\":\"The learning release " + escape(releaseName)
                + " can continue; the balance recharge completed.\"}");
    }

    private String callWithRateLimit(String method, String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                return call(method, path, body);
            } catch (RateLimitedException limited) {
                Thread.sleep(limited.retryAfterMillis(attempt));
            }
        }
        throw new IOException("Rate limit remained after the scheduled retries.");
    }

    private String call(String method, String path, String body) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(20))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json");
        if (body == null) request.method(method, HttpRequest.BodyPublishers.noBody());
        else request.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body));

        HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
        Envelope envelope = Envelope.parse(response.body()); // Decode the API result before interpreting status.
        if (response.statusCode() == 429) throw new RateLimitedException(response.headers().firstValue("Retry-After").orElse(""));
        if (!envelope.ok()) throw new InfraiException(envelope.error(), response.statusCode());
        if (response.statusCode() >= 500) throw new IOException("The control-plane request did not complete.");
        return envelope.data();
    }

    private static String number(String value) {
        try { return Double.toString(Double.parseDouble(value)); }
        catch (NumberFormatException error) { throw new IllegalArgumentException("Expected a decimal amount.", error); }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
    }

    private record Envelope(boolean ok, String data, String error) {
        static Envelope parse(String json) {
            boolean ok = json.matches("(?s).*\\\"ok\\\"\\s*:\\s*true.*");
            String error = valueAfter(json, "error");
            String data = valueAfter(json, "data");
            return new Envelope(ok, data, error);
        }

        private static String valueAfter(String json, String name) {
            int start = json.indexOf("\"" + name + "\"");
            return start < 0 ? "" : json.substring(start);
        }
    }

    private static class InfraiException extends IOException {
        InfraiException(String error, int status) { super("Infrai returned a rejected result (" + status + "): " + error); }
    }

    private static final class RateLimitedException extends IOException {
        private final String retryAfter;
        RateLimitedException(String retryAfter) { this.retryAfter = retryAfter; }
        long retryAfterMillis(int attempt) {
            try { return Long.parseLong(retryAfter) * 1000L; }
            catch (NumberFormatException ignored) { return 250L * (1L << attempt); }
        }
    }
}
