package io.github.lucasssoh.boussole.server;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.lucasssoh.boussole.adapters.network.LocalCertificate;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Another device on the network: nothing without pairing, and only what its access allows. */
class NetworkAccessTest {

    static {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host");
    }

    private static final Pattern CODE = Pattern.compile("\"code\":\"(\\d{6})\"");
    private static final Pattern ID = Pattern.compile("\"id\":\"([\\w-]+)\"");
    private static final Pattern KEY = Pattern.compile("boussole_device=([\\w-]+)");

    @TempDir
    static Path data;

    private static RunningServer server;
    private static int networkPort;
    private final HttpClient local = HttpClient.newHttpClient();
    private final HttpClient device = trustingClient();

    @BeforeAll
    static void start() throws IOException {
        try (ServerSocket free = new ServerSocket(0)) {
            networkPort = free.getLocalPort();
        }
        LocalCertificate certificate = LocalCertificate.ensure(data.resolve("network"), List.of("192.168.1.14"));
        NetworkSettings network = new NetworkSettings(networkPort, certificate.keystore(), certificate.password(),
                List.of("192.168.1.14"), certificate.fingerprint());
        server = BoussoleServer.start(new ServerSettings(0, () -> List.of(Path.of("../examples/reseaux")), List.of(),
                data.resolve("state.db"), Optional.empty(), false, Optional.of(network)));
    }

    @AfterAll
    static void stop() {
        server.close();
    }

    @Test
    void an_unpaired_device_gets_the_interface_but_nothing_of_the_api() throws Exception {
        assertThat(fromDevice("GET", "/", null, null).statusCode()).isEqualTo(200);
        HttpResponse<String> refused = fromDevice("GET", "/api/subjects", null, null);

        assertThat(refused.statusCode()).isEqualTo(401);
        assertThat(refused.body()).doesNotContain("reseaux");
    }

    @Test
    void the_network_never_manages_devices_nor_the_server() throws Exception {
        assertThat(fromDevice("GET", "/api/devices", null, null).statusCode()).isEqualTo(403);
        assertThat(fromDevice("POST", "/api/server/stop", "", null).statusCode()).isEqualTo(403);
    }

    @Test
    void another_host_name_is_refused() throws Exception {
        HttpResponse<String> rebound = device.send(HttpRequest.newBuilder(deviceUri("/"))
                .header("Host", "evil.example:" + networkPort).build(), HttpResponse.BodyHandlers.ofString());

        assertThat(rebound.statusCode()).isEqualTo(403);
    }

    @Test
    void a_device_paired_with_the_code_reads_and_answers_until_removed() throws Exception {
        String code = group(CODE, fromComputer("POST", "/api/devices/pairing", "{\"access\":\"full\"}").body());
        assertThat(fromDevice("POST", "/api/pairing/preview", "{\"code\":\"" + code + "\"}", null).body()).contains("\"access\":\"full\"");

        HttpResponse<String> claimed = fromDevice("POST", "/api/pairing/claim", "{\"code\":\"" + code + "\"}", null);
        String key = group(KEY, claimed.headers().firstValue("set-cookie").orElseThrow());

        assertThat(claimed.headers().firstValue("set-cookie").orElseThrow()).contains("HttpOnly", "Secure", "SameSite=Strict");
        assertThat(claimed.body()).doesNotContain(key);
        assertThat(fromDevice("GET", "/api/subjects", null, key).statusCode()).isEqualTo(200);
        assertThat(fromDevice("POST", "/api/subjects/reseaux/exercises/s11-a1/attempts?session=s11",
                "{\"kind\":\"value\",\"value\":\"48\"}", key).statusCode()).isEqualTo(200);
        assertThat(fromDevice("POST", "/api/pairing/claim", "{\"code\":\"" + code + "\"}", null).statusCode()).isEqualTo(409);

        String id = group(ID, fromComputer("GET", "/api/devices", null).body().split("\"devices\"")[1]);
        assertThat(fromComputer("DELETE", "/api/devices/" + id, null).statusCode()).isEqualTo(200);
        assertThat(fromDevice("GET", "/api/subjects", null, key).statusCode()).isEqualTo(401);
    }

    @Test
    void a_device_that_asked_is_paired_once_the_owner_accepts_read_only() throws Exception {
        HttpResponse<String> asked = fromDevice("POST", "/api/pairing/requests", "", null);
        String id = group(ID, asked.body());
        String code = group(CODE, asked.body());
        assertThat(fromDevice("GET", "/api/pairing/requests/" + id, null, null).body()).contains("\"state\":\"pending\"");
        assertThat(fromComputer("GET", "/api/devices", null).body()).contains("\"code\":\"" + code + "\"").doesNotContain(id);

        fromComputer("POST", "/api/devices/requests/" + code + "/accept", "{\"access\":\"read-only\"}");
        HttpResponse<String> accepted = fromDevice("GET", "/api/pairing/requests/" + id, null, null);
        String key = group(KEY, accepted.headers().firstValue("set-cookie").orElseThrow());

        assertThat(accepted.body()).contains("\"state\":\"accepted\"", "\"access\":\"read-only\"");
        assertThat(fromDevice("GET", "/api/subjects", null, key).statusCode()).isEqualTo(200);
        assertThat(fromDevice("POST", "/api/subjects/reseaux/exercises/s11-a2/attempts",
                "{\"kind\":\"choices\",\"picked\":[1]}", key).statusCode()).isEqualTo(403);
    }

    @Test
    void this_computer_cannot_pair_itself_on_its_own_port() throws Exception {
        String code = group(CODE, fromComputer("POST", "/api/devices/pairing", "{\"access\":\"full\"}").body());

        assertThat(fromComputer("POST", "/api/pairing/claim", "{\"code\":\"" + code + "\"}").statusCode()).isEqualTo(409);
    }

    private HttpResponse<String> fromComputer(String method, String path, String json) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + server.port() + path))
                .header("Content-Type", "application/json").header("X-Boussole", "1");
        request.method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        return local.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> fromDevice(String method, String path, String json, String key) throws IOException, InterruptedException {
        HttpRequest.Builder request = HttpRequest.newBuilder(deviceUri(path)).header("Host", "192.168.1.14:" + networkPort);
        if (json != null) {
            request.header("Content-Type", "application/json").header("X-Boussole", "1");
        }
        if (key != null) {
            request.header("Cookie", "boussole_device=" + key);
        }
        request.method(method, json == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(json));
        return device.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static URI deviceUri(String path) {
        return URI.create("https://localhost:" + networkPort + path);
    }

    private static String group(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        assertThat(matcher.find()).as("%s in %s", pattern, text).isTrue();
        return matcher.group(1);
    }

    /** The test trusts the certificate made for it, as a device whose owner accepted it. */
    private static HttpClient trustingClient() {
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[] {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String type) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String type) {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }}, new SecureRandom());
            return HttpClient.newBuilder().sslContext(context).build();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
