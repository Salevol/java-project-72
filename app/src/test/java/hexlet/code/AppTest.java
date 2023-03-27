package hexlet.code;

import hexlet.code.domain.Url;
import hexlet.code.domain.query.QUrl;
import io.ebean.DB;
import io.ebean.Database;
import io.javalin.Javalin;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.assertj.core.api.Assertions.assertThat;


public final class AppTest {

    @Test
    void testInit() {
        assertThat(true).isEqualTo(true);
    }

    private static Javalin app;
    private static String baseUrl;
    private static Url existingUrl = new Url("https://www.testexample.com");
    private static Database database;
    private static MockWebServer server;
    private static final String MOCK_HTML = "src/test/resources/mock_index.html";

    @BeforeAll
    public static void beforeAll() throws IOException {
        app = App.getApp();
        app.start(0);
        int port = app.port();
        baseUrl = "http://localhost:" + port;
        database = DB.getDefault();
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    public static void afterAll() {
        app.stop();
    }

    @BeforeEach
    void beforeEach() {
        database.script().run("/truncate.sql");
        database.script().run("/seed-test-db.sql");
    }

    @Nested
    class RootTest {

        @Test
        void testIndex() {
            HttpResponse<String> response = Unirest.get(baseUrl).asString();
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.getBody()).contains("Webpage analyzer");
        }

    }

    @Nested
    class UrlTest {
        @Test
        void testCreate() {
            String testUrl = "https://stackoverflow.com";
            HttpResponse<String> responsePost = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", testUrl)
                    .asEmpty();

            assertThat(responsePost.getStatus()).isEqualTo(HTTP_MOVED_TEMP);
            assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

            HttpResponse<String> response = Unirest
                    .get(baseUrl + "/urls")
                    .asString();
            String body = response.getBody();

            assertThat(response.getStatus()).isEqualTo(HTTP_OK);
            assertThat(body).contains(testUrl);
            assertThat(body).contains("Page successfully added");

            Url actualUrl = new QUrl()
                    .name.equalTo(testUrl)
                    .findOne();

            assertThat(actualUrl).isNotNull();
            assertThat(actualUrl.getName()).isEqualTo(testUrl);

            String invalidUrl = "invalidUrl";
            HttpResponse<String> responsePostInvalid = Unirest
                    .post(baseUrl + "/urls")
                    .field("url", invalidUrl)
                    .asEmpty();
            assertThat(responsePostInvalid.getStatus()).isEqualTo(HTTP_MOVED_TEMP);
            assertThat(responsePostInvalid.getHeaders().getFirst("Location")).isEqualTo("/");
        }

        @Test
        void testShowUrl() {
            HttpResponse<String> response = Unirest.get(baseUrl + "/urls/1").asString();
            assertThat(response.getStatus()).isEqualTo(HttpURLConnection.HTTP_OK);
            assertThat(response.getBody()).contains(existingUrl.getName());

            HttpResponse<String> responseNotFound = Unirest.get(baseUrl + "/urls/404").asString();
            assertThat(responseNotFound.getStatus()).isEqualTo(HttpURLConnection.HTTP_NOT_FOUND);
            assertThat(responseNotFound.getBody()).contains("404: Id not found");
        }
    }

    @Test
    void testUrlCheck() throws IOException {
        String testPage = Files.readString(Paths.get(MOCK_HTML));

        String mockUrl = server.url("/").toString();
        mockUrl = mockUrl.substring(0, mockUrl.length() - 1);
        server.enqueue(new MockResponse().setBody(testPage));

        HttpResponse<String> responsePost = Unirest
                .post(baseUrl + "/urls")
                .field("url", mockUrl)
                .asEmpty();

        assertThat(responsePost.getStatus()).isEqualTo(HTTP_MOVED_TEMP);
        assertThat(responsePost.getHeaders().getFirst("Location")).isEqualTo("/urls");

        Long mockUrlId = new QUrl()
                .name.equalTo(mockUrl)
                .findOneOrEmpty()
                .get()
                .getId();

        assertThat(mockUrlId).isNotNull();

        HttpResponse<String> checkUrl = Unirest
                .post(baseUrl + "/urls/" + mockUrlId + "/checks/")
                .asEmpty();

        assertThat(checkUrl.getStatus()).isEqualTo(HttpURLConnection.HTTP_MOVED_TEMP);
        assertThat(checkUrl.getHeaders().getFirst("Location")).isEqualTo("/urls/" + mockUrlId);

        HttpResponse<String> urlsResponse = Unirest
                .get(baseUrl + "/urls/" + mockUrlId)
                .asString();

        assertThat(urlsResponse.getBody()).contains(mockUrl);
        assertThat(urlsResponse.getBody()).contains("Mockpage description");
        assertThat(urlsResponse.getBody()).contains("Mockpage title");
        assertThat(urlsResponse.getBody()).contains("First-level header");
    }
}
