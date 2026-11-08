package com.example.condosched.repository;

import com.example.condosched.domain.DataError;
import com.example.condosched.domain.Forecast;
import com.example.condosched.network.WeatherApi;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import static org.junit.Assert.*;

public class WeatherRepositoryTest {
    private MockWebServer server;
    private WeatherRepository repository;
    private WeatherApi api;
    private static final String FORECAST = "{\"forecast\":{\"forecastday\":["
            + "{\"date\":\"2026-11-08\",\"day\":{\"condition\":{\"code\":1000,\"text\":\"Sol; céu limpo\"}}},"
            + "{\"date\":\"2026-11-09\",\"day\":{\"condition\":{\"code\":1183,\"text\":\"Chuva\"}}}]}}";

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        api = new Retrofit.Builder().baseUrl(server.url("/v1/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(WeatherApi.class);
        repository = new WeatherRepository(api, "test-key");
    }

    @After
    public void close() throws Exception {
        repository.cancel();
        server.shutdown();
    }

    @Test
    public void sendsCoordinatesLanguageAndDaysAndConvertsJsonIntoForecast() throws Exception {
        server.enqueue(new MockResponse().setBody(FORECAST));
        Result result = new Result();
        repository.load(40.0, -7.0, 7, "pt", result);
        result.await();
        assertNull(result.error);
        assertEquals(2, result.data.size());
        assertEquals(LocalDate.of(2026, 11, 8), result.data.get(0).getDate());
        assertEquals(1000, result.data.get(0).getConditionCode());
        assertEquals("Sol; céu limpo", result.data.get(0).getDescription());
        RecordedRequest request = server.takeRequest();
        assertEquals("GET", request.getMethod());
        assertEquals("/v1/forecast.json", request.getRequestUrl().encodedPath());
        assertEquals("test-key", request.getRequestUrl().queryParameter("key"));
        assertEquals("40.0,-7.0", request.getRequestUrl().queryParameter("q"));
        assertEquals("7", request.getRequestUrl().queryParameter("days"));
        assertEquals("pt", request.getRequestUrl().queryParameter("lang"));
        assertNull(request.getHeader("apikey"));
    }

    @Test
    public void missingOrInvalidCoordinatesUsePortoAndRespectRequestedDayLimit() throws Exception {
        for (Double latitude : new Double[]{null, Double.NaN, 91.0}) {
            server.enqueue(new MockResponse().setBody(FORECAST));
            Result result = new Result();
            repository.load(latitude, -7.0, 1, "en", result);
            result.await();
            assertNull(result.error);
            assertEquals(1, result.data.size());
            assertEquals("41.15,-8.61024", server.takeRequest().getRequestUrl().queryParameter("q"));
        }
    }

    @Test
    public void incompleteOrInvalidForecastIsRejected() throws Exception {
        for (String json : new String[]{"null", "{}", "{\"forecast\":{}}",
                FORECAST.replace("2026-11-08", "invalid"),
                FORECAST.replace("\"code\":1000,", ""),
                FORECAST.replace("\"text\":\"Chuva\"", "\"text\":\"\"")}) {
            server.enqueue(new MockResponse().setBody(json));
            Result result = new Result();
            repository.load(null, null, 7, "pt", result);
            result.await();
            assertEquals(DataError.INVALID_RESPONSE, result.error);
            assertNull(result.data);
        }
    }

    @Test
    public void httpFailureAndMalformedJsonDoNotBecomeAnEmptySuccessfulForecast() throws Exception {
        for (MockResponse response : new MockResponse[]{new MockResponse().setResponseCode(503),
                new MockResponse().setBody("not-json")}) {
            server.enqueue(response);
            Result result = new Result();
            repository.load(null, null, 7, "en", result);
            result.await();
            assertEquals(DataError.LOAD_FAILED, result.error);
            assertNull(result.data);
        }
    }

    @Test
    public void emptyForecastIsAValidEmptyResult() throws Exception {
        server.enqueue(new MockResponse().setBody("{\"forecast\":{\"forecastday\":[]}}"));
        Result result = new Result();
        repository.load(null, null, 7, "en", result);
        result.await();
        assertNull(result.error);
        assertTrue(result.data.isEmpty());
    }

    @Test
    public void cancellingTheRequestSuppressesItsCallbacks() throws Exception {
        server.enqueue(new MockResponse().setBody(FORECAST).setBodyDelay(300, TimeUnit.MILLISECONDS));
        Result result = new Result();
        repository.load(null, null, 7, "en", result);
        assertNotNull(server.takeRequest(2, TimeUnit.SECONDS));
        repository.cancel();
        assertFalse(result.done.await(600, TimeUnit.MILLISECONDS));
    }

    @Test
    public void missingKeyIsReportedWithoutSendingARequest() throws Exception {
        WeatherRepository unconfigured = new WeatherRepository(api, "");
        Result result = new Result();
        unconfigured.load(null, null, 7, "en", result);
        result.await();
        assertEquals(DataError.SERVICE_UNAVAILABLE, result.error);
        assertEquals(0, server.getRequestCount());
    }

    private static class Result implements RepositoryCallback<List<Forecast>> {
        final CountDownLatch done = new CountDownLatch(1);
        List<Forecast> data;
        DataError error;

        @Override
        public void onSuccess(List<Forecast> forecasts) { data = forecasts; done.countDown(); }
        @Override
        public void onError(DataError error) { this.error = error; done.countDown(); }
        void await() throws InterruptedException { assertTrue(done.await(5, TimeUnit.SECONDS)); }
    }
}
