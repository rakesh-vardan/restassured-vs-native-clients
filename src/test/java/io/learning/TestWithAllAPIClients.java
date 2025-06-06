package io.learning;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ProtocolException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;

public class TestWithAllAPIClients {

    public final String LEANNE_GRAHAM = "Leanne Graham";
    public final int STATUS_OK = 200;
    public final String CONTENT_TYPE_KEY = "Content-Type";
    public final String CONTENT_TYPE_VALUE = "application/json; charset=utf-8";
    private final String URL = "https://jsonplaceholder.typicode.com/users/1";

    @Test
    void testWithHttpURLConnection() throws IOException {
        // prepare request
        URL url = new URL(this.URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // send request
        connection.connect();

        // validate response
        assertEquals(STATUS_OK, connection.getResponseCode());
        assertEquals(CONTENT_TYPE_VALUE, connection
                .getHeaderField(CONTENT_TYPE_KEY));

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        StringBuilder response = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        assertTrue(response.toString().contains(LEANNE_GRAHAM));
        connection.disconnect();
    }

    @Test
    void testWithHttpClient() throws IOException, InterruptedException {
        // prepare request
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.URL))
                .build();

        // send request
        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        // validate response
        assertEquals(200, response.statusCode());
        assertEquals(CONTENT_TYPE_VALUE, response.headers()
                .firstValue(CONTENT_TYPE_KEY).get());
        assertTrue(response.body().contains(LEANNE_GRAHAM));
    }


    @Test
    void testWithApacheHttpClient() throws ProtocolException, IOException {
        // prepare request
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(this.URL);

            // send request
            try (CloseableHttpResponse response = httpClient.execute(request)) {

                // validate response
                assertEquals(200, response.getCode());
                assertEquals(CONTENT_TYPE_VALUE,
                        response.getHeader(CONTENT_TYPE_KEY).getValue());
                assertTrue(EntityUtils.toString(response.getEntity())
                        .contains(LEANNE_GRAHAM));
            }
        }
    }

    @Test
    void testWithSpringRestTemplate() {
        // prepare and send request
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(this.URL, String.class);

        // validate response
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(CONTENT_TYPE_VALUE, response.getHeaders()
                .getFirst(CONTENT_TYPE_KEY));
        assertTrue(response.getBody().contains(LEANNE_GRAHAM));
    }

    @Test
    void testWithSpringWebClient() {
        // prepare and send request
        WebClient webClient = WebClient.create();
        ClientResponse response = webClient.get()
                .uri(this.URL)
                .exchange()
                .block();

        // validate response
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.statusCode());
        assertEquals("application/json;charset=utf-8",
                Objects.requireNonNull(response.headers().asHttpHeaders().getContentType())
                        .toString());
        assertTrue(Objects.requireNonNull(response.bodyToMono(String.class).block())
                .contains(LEANNE_GRAHAM));
    }

    @Test
    void testWithRestAssured() {
        given().
                baseUri(this.URL).       // prepare request
        when()
                .get().                  // send request
        then()
                .statusCode(200).and() // validate response
                .body(containsString(LEANNE_GRAHAM)).and()
                .header(CONTENT_TYPE_KEY, CONTENT_TYPE_VALUE);
    }
}
