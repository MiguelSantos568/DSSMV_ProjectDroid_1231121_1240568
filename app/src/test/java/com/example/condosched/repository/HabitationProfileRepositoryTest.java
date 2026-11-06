package com.example.condosched.repository;

import com.example.condosched.network.HabitationProfileApi;
import com.example.condosched.domain.DataError;

import com.example.condosched.domain.HabitationProfile;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HabitationProfileRepositoryTest {
    private MockWebServer server;
    private HabitationProfileRepository service;
    private static final String PROFILE = "[{\"unit_id\":1,\"full_name\":\"Contacto existente\","
            + "\"email\":\"casa@example.com\"}]";

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        HabitationProfileApi api = new Retrofit.Builder().baseUrl(server.url("/rest/v1/"))
                .addConverterFactory(GsonConverterFactory.create()).build()
                .create(HabitationProfileApi.class);
        service = new HabitationProfileRepository(api);
    }

    @After
    public void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    public void missingProfileAllowsFirstRegistration() throws Exception {
        server.enqueue(new MockResponse().setBody("[]"));
        Result result = new Result();
        service.find(1, result);
        result.await();
        assertNull(result.error);
        assertNull(result.profile);
        RecordedRequest request = server.takeRequest();
        assertEquals("/rest/v1/rpc/get_habitation_profile", request.getPath());
        JsonObject body = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals(1, body.get("p_unit_id").getAsLong());
        assertFalse(body.has("p_full_name"));
    }

    @Test
    public void repeatScanReturnsTheExistingSharedProfile() throws Exception {
        server.enqueue(new MockResponse().setBody(PROFILE));
        Result result = new Result();
        service.find(1, result);
        result.await();
        assertNull(result.error);
        assertEquals("Contacto existente", result.profile.getFullName());
        assertEquals("casa@example.com", result.profile.getEmail());
        assertEquals(1, server.getRequestCount()); // No create request on lookup.
    }

    @Test
    public void concurrentRegistrationUsesProfileReturnedByServer() throws Exception {
        server.enqueue(new MockResponse().setBody(PROFILE));
        Result result = new Result();
        service.create(1, "Outro contacto", "outro@example.com", result);
        result.await();
        assertNull(result.error);
        assertEquals("Contacto existente", result.profile.getFullName());
        RecordedRequest request = server.takeRequest();
        assertEquals("/rest/v1/rpc/create_habitation_profile", request.getPath());
        JsonObject body = JsonParser.parseString(request.getBody().readUtf8()).getAsJsonObject();
        assertEquals("Outro contacto", body.get("p_full_name").getAsString());
        assertEquals("outro@example.com", body.get("p_email").getAsString());
    }

    @Test
    public void serverFailureDoesNotMeanAnUnregisteredHome() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(503).setBody("{}"));
        Result result = new Result();
        service.find(1, result);
        result.await();
        assertNotNull(result.error);
        assertFalse(result.success);
    }

    @Test
    public void missingDatabaseSetupDoesNotAllowLocalOnlyRegistration() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("{}"));
        Result result = new Result();
        service.find(1, result);
        result.await();
        assertNotNull(result.error);
        assertFalse(result.success);
    }

    @Test
    public void rejectsProfileForAnotherHome() throws Exception {
        server.enqueue(new MockResponse().setBody(PROFILE));
        Result result = new Result();
        service.find(2, result);
        result.await();
        assertNotNull(result.error);
        assertFalse(result.success);
    }

    @Test
    public void emptyCreateResponseDoesNotCompleteRegistration() throws Exception {
        server.enqueue(new MockResponse().setBody("[]"));
        Result result = new Result();
        service.create(1, "Contacto", "casa@example.com", result);
        result.await();
        assertNotNull(result.error);
        assertFalse(result.success);
    }

    private static class Result implements RepositoryCallback<HabitationProfile> {
        final CountDownLatch done = new CountDownLatch(1);
        HabitationProfile profile;
        DataError error;
        boolean success;

        @Override
        public void onSuccess(HabitationProfile profile) {
            this.profile = profile;
            success = true;
            done.countDown();
        }

        @Override
        public void onError(DataError message) {
            error = message;
            done.countDown();
        }

        void await() throws InterruptedException {
            assertTrue("The network callback did not finish", done.await(5, TimeUnit.SECONDS));
        }
    }
}
