package com.example.condosched.repository;

import com.example.condosched.domain.*;
import com.example.condosched.network.CondominiumApi;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.*;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class HabitationRepositoryTest {
    private MockWebServer server;
    private HabitationRepository repository;

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        repository = new HabitationRepository(new Retrofit.Builder().baseUrl(server.url("/api/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(CondominiumApi.class));
    }

    @After
    public void close() throws Exception {
        server.shutdown();
    }

    @Test
    public void findsRequestedHomeAndConvertsTheResponse() throws Exception {
        server.enqueue(new MockResponse().setBody("[{\"id\":7,\"name\":\"Condo\",\"buildings\":["
                + "{\"id\":8,\"name\":\"Building\",\"units\":[{\"id\":1,\"floor\":2,\"unitNumber\":\"201\"}]}]}]"));
        Result result = new Result();
        repository.find(1, result);
        result.await();
        assertNull(result.error);
        assertEquals(7, result.home.condominiumId);
        assertEquals(8, result.home.buildingId);
        assertEquals("201", result.home.unit);
    }

    @Test
    public void absentHomeIsDistinctFromNetworkFailure() throws Exception {
        server.enqueue(new MockResponse().setBody("[]"));
        Result result = new Result();
        repository.find(1, result);
        result.await();
        assertEquals(DataError.HOME_UNAVAILABLE, result.error);
    }

    @Test
    public void httpFailureDoesNotAllowRegistration() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(503));
        Result result = new Result();
        repository.find(1, result);
        result.await();
        assertEquals(DataError.LOAD_FAILED, result.error);
        assertNull(result.home);
    }

    private static class Result implements RepositoryCallback<Habitation> {
        final CountDownLatch done = new CountDownLatch(1);
        Habitation home;
        DataError error;

        @Override
        public void onSuccess(Habitation home) {
            this.home = home;
            done.countDown();
        }

        @Override
        public void onError(DataError error) {
            this.error = error;
            done.countDown();
        }

        void await() throws Exception {
            assertTrue(done.await(5, TimeUnit.SECONDS));
        }
    }
}
