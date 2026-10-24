package io.github.lucasssoh.boussole.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** The HTTP API on the example subject, through a real server. */
class ServerApiTest {

    static {
        System.setProperty("jdk.httpclient.allowRestrictedHeaders", "host");
    }

    @TempDir
    static Path data;

    private static RunningServer server;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeAll
    static void start() {
        server = BoussoleServer.start(new ServerSettings(
                0, () -> List.of(Path.of("../examples/reseaux")), List.of(), data.resolve("state.db"), Optional.empty(), false));
    }

    @AfterAll
    static void stop() {
        server.close();
    }

    private HttpResponse<String> get(String path) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> post(String path, String json) throws IOException, InterruptedException {
        return client.send(HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .header("X-Boussole", "1")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build(), HttpResponse.BodyHandlers.ofString());
    }

    private static URI uri(String path) {
        return URI.create("http://localhost:" + server.port() + path);
    }

    @Test
    void lists_the_subjects() throws Exception {
        assertThat(get("/api/subjects").body()).contains("\"id\":\"reseaux\"", "\"written\":2");
    }

    @Test
    void a_session_never_carries_answers_nor_solutions() throws Exception {
        HttpResponse<String> session = get("/api/subjects/reseaux/sessions/s11");

        assertThat(session.statusCode()).isEqualTo(200);
        assertThat(session.body())
                .contains("\"type\":\"mcq\"", "\"kind\":\"notion\"", "Circuit virtuel")
                .doesNotContain("Le circuit virtuel : chaque routeur", "Non : en datagramme", "\"correct\":");
    }

    @Test
    void a_review_shows_the_questions_it_asks_again() throws Exception {
        assertThat(get("/api/subjects/reseaux/sessions/r19").body()).contains("\"id\":\"s11-a1\"", "\"reused\":true");
    }

    @Test
    void an_attempt_is_checked_then_the_solution_opens() throws Exception {
        assertThat(get("/api/subjects/reseaux/exercises/s11-w1/solution").statusCode()).isEqualTo(409);

        HttpResponse<String> attempt = post("/api/subjects/reseaux/exercises/s11-w1/attempts",
                "{\"kind\":\"value\",\"value\":\"  Le Forwarding \"}");

        assertThat(attempt.body()).contains("\"verdict\":\"correct\"", "\"attempts\":1", "consulte la table");
        assertThat(get("/api/subjects/reseaux/exercises/s11-w1/solution").body())
                .contains("\"kind\":\"value\"", "forwarding", "consulte la table");
    }

    @Test
    void reports_points_ranks_and_streak() throws Exception {
        post("/api/subjects/reseaux/exercises/s11-a1/attempts?session=s11", "{\"kind\":\"value\",\"value\":\"48\"}");

        assertThat(get("/api/stats").body()).contains("\"points\":", "\"culture\":{\"tier\":\"curious\",\"division\":5,\"level\":1",
                "\"domain\":\"information-technology\"", "\"specialty\":{\"tier\":\"novice\"", "\"streakDays\":",
                "\"records\":{\"bestStreak\":", "\"route\":[{\"tier\":\"curious\",\"division\":5,\"from\":0.0,\"firstLevel\":1}");
        assertThat(get("/api/ranks").body()).contains("{\"tier\":\"grand-sage\",\"division\":0,\"from\":404845.0,\"firstLevel\":491}",
                "{\"tier\":\"luminary\",\"division\":0,\"from\":80000.0,\"firstLevel\":421}");
    }

    @Test
    void a_wrong_choice_brings_its_feedback() throws Exception {
        assertThat(post("/api/subjects/reseaux/exercises/s11-a2/attempts", "{\"kind\":\"choices\",\"picked\":[0]}").body())
                .contains("\"verdict\":\"wrong\"", "Non : en datagramme");
    }

    @Test
    void hints_come_one_at_a_time() throws Exception {
        assertThat(post("/api/subjects/reseaux/exercises/s11-p1/hints", "").body()).contains("\"number\":1", "\"total\":1");
        assertThat(post("/api/subjects/reseaux/exercises/s11-p1/hints", "").statusCode()).isEqualTo(409);
    }

    @Test
    void the_subject_resumes_in_the_session_answered_in() throws Exception {
        post("/api/subjects/reseaux/exercises/s11-a1/attempts?session=r19", "{\"kind\":\"value\",\"value\":\"48\"}");

        assertThat(get("/api/subjects/reseaux").body()).contains("\"resume\":{\"id\":\"r19\"");
    }

    @Test
    void the_choices_of_an_mcq_are_shown_shuffled_the_same_way_each_time() throws Exception {
        String session = get("/api/subjects/reseaux/sessions/s11").body();
        int mcq = session.indexOf("\"id\":\"s11-a2\"");
        String order = session.substring(session.indexOf("\"order\":", mcq), session.indexOf("]", session.indexOf("\"order\":", mcq)) + 1);

        assertThat(order).doesNotContain("[0,1,2]").doesNotContain("[0,1]").doesNotContain("[0,1,2,3]");
        assertThat(get("/api/subjects/reseaux/sessions/s11").body()).contains(order);
    }

    @Test
    void a_session_says_which_prerequisites_it_waits_for() throws Exception {
        assertThat(get("/api/subjects/reseaux").body())
                .contains("\"waitingFor\":[{\"subject\":\"reseaux\",\"session\":\"s11\",\"title\":\"Rôle de la couche réseau\"}]");
    }

    @Test
    void a_finished_session_shows_in_the_course() throws Exception {
        assertThat(post("/api/subjects/reseaux/sessions/s11/finish", "{\"spentSeconds\":3000}").body()).contains("\"bonus\":0.0");
        assertThat(get("/api/subjects/reseaux").body()).contains("\"id\":\"s11\"", "\"finished\":true");
    }

    @Test
    void serves_the_documents_of_the_subject_only() throws Exception {
        assertThat(get("/api/subjects/reseaux/files/sources/Reseaux_Cours_Intro_2026.pdf").headers().firstValue("Content-Type"))
                .contains("application/pdf");
        assertThat(get("/api/subjects/reseaux/files/../../pom.xml").statusCode()).isEqualTo(404);
        assertThat(get("/api/subjects/reseaux/files/%2E%2E/%2E%2E/pom.xml").statusCode()).isEqualTo(404);
    }

    @Test
    void refuses_a_post_without_the_boussole_header() throws Exception {
        HttpResponse<String> forged = client.send(HttpRequest.newBuilder(uri("/api/subjects/reseaux/exercises/s11-a1/attempts"))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString("{\"kind\":\"value\",\"value\":\"48\"}"))
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(forged.statusCode()).isEqualTo(403);
    }

    @Test
    void refuses_another_host_name() throws Exception {
        HttpResponse<String> rebound = client.send(HttpRequest.newBuilder(uri("/api/subjects"))
                .header("Host", "evil.example:" + server.port())
                .build(), HttpResponse.BodyHandlers.ofString());

        assertThat(rebound.statusCode()).isEqualTo(403);
    }
}
