package com.example.condosched.repository;

import com.example.condosched.domain.Amenity;
import com.example.condosched.domain.DataError;
import com.example.condosched.repository.RepositoryCallback;
import com.example.condosched.network.AmenityApi;

import java.time.LocalTime;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.util.List;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class AmenityRepositoryTest {
    private MockWebServer server;
    private AmenityRepository service;

    @Before
    public void setup() throws Exception {
        server = new MockWebServer();
        server.start();
        AmenityApi api = new Retrofit.Builder().baseUrl(server.url("/api/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(AmenityApi.class);
        service = new AmenityRepository(api);
    }

    @After
    public void teardown() throws Exception {
        server.shutdown();
    }

    @Test
    public void filtersByCondominiumAndMapsRealApiTimeAndDecimalFee() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("[{\"id\":1,\"buildingId\":7,"
                + "\"isActive\":true,\"name\":\"Piscina\",\"capacity\":50,"
                + "\"openingTime\":\"08:00:00\",\"closingTime\":\"20:00:00\","
                + "\"bookingFee\":2.5,\"requiresBooking\":true}]"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertFalse(result.error);
        assertEquals(1, result.amenities.size());
        Amenity amenity = result.amenities.get(0);
        assertEquals(LocalTime.of(8, 0), amenity.getOpeningTime());
        assertEquals(LocalTime.of(20, 0), amenity.getClosingTime());
        assertEquals(2.5, amenity.getBookingFee(), 0.001);
        assertTrue(amenity.isRequiresBooking());
        assertEquals("/api/buildings/condominium/7", server.takeRequest().getPath());
        assertEquals("/api/amenities?condominiumId=7", server.takeRequest().getPath());
    }

    @Test
    public void includesOtherBuildingsInSameCondominiumButExcludesOtherCondominiums() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("[null,"
                + "{\"id\":1,\"buildingId\":7,\"isActive\":true},"
                + "{\"id\":2,\"buildingId\":7,\"isActive\":false},"
                + "{\"id\":3,\"buildingId\":8,\"isActive\":true},"
                + "{\"id\":4,\"buildingId\":9,\"isActive\":true}]"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertFalse(result.error);
        assertEquals(2, result.amenities.size());
        assertEquals(1, result.amenities.get(0).getId());
        assertEquals(3, result.amenities.get(1).getId());
        assertEquals("Building B", result.amenities.get(1).getBuildingName());
    }

    @Test
    public void emptyListIsSuccessful() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("[]"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertFalse(result.error);
        assertTrue(result.amenities.isEmpty());
    }

    @Test
    public void serverErrorIsNotAnEmptyList() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setResponseCode(503).setBody("{}"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertTrue(result.error);
        assertNull(result.amenities);
    }

    @Test
    public void malformedResponseReportsAnError() throws Exception {
        enqueueBuildings();
        server.enqueue(new MockResponse().setBody("not JSON"));
        Result result = new Result();
        service.getByCondominium(7, result);
        result.await();
        assertTrue(result.error);
    }

    @Test
    public void missingCondominiumNeverRequestsAllAmenities() throws Exception {
        Result result = new Result();
        service.getByCondominium(0, result);
        result.await();
        assertTrue(result.error);
        assertEquals(0, server.getRequestCount());
    }

    private void enqueueBuildings() {
        server.enqueue(new MockResponse().setBody("["
                + "{\"id\":7,\"condominiumId\":7,\"name\":\"Building A\"},"
                + "{\"id\":8,\"condominiumId\":7,\"name\":\"Building B\"},"
                + "{\"id\":9,\"condominiumId\":99,\"name\":\"Other condominium\"}]"));
    }

    private static class Result implements RepositoryCallback<List<Amenity>> {
        final CountDownLatch done = new CountDownLatch(1);
        List<Amenity> amenities;

        boolean error;

        @Override
        public void onSuccess(List<Amenity> amenities) {
            this.amenities = amenities;

            done.countDown();
        }

        @Override
        public void onError(DataError reason) {
            error = true;
            done.countDown();
        }

        void await() throws InterruptedException {
            assertTrue("The amenities request did not finish", done.await(5, TimeUnit.SECONDS));
        }
    }
}
