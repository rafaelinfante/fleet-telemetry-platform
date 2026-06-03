package net.rafaelinfante.fleet.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import net.rafaelinfante.fleet.support.AbstractIntegrationTest;

/**
 * Drives the REST API and actuator through MockMvc against the real database, covering the
 * seeded data, RFC 9457 error responses, validation and the custom Prometheus metrics.
 */
@AutoConfigureMockMvc
@AutoConfigureObservability
class WebApiIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private PrometheusMeterRegistry prometheusRegistry;

    @Test
    void seededRulesAndGeofencesAreExposed() throws Exception {
        mvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(8)));
        mvc.perform(get("/api/geofences"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(2)));
    }

    @Test
    void unknownDeviceReturnsProblemDetail() throws Exception {
        mvc.perform(get("/api/devices/DOES-NOT-EXIST"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    void invalidGeofenceIsRejected() throws Exception {
        mvc.perform(post("/api/geofences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Bad\",\"centerLat\":53.3,\"centerLng\":-6.2,\"radiusM\":-5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void ruleLifecycle() throws Exception {
        String body = """
                {"name":"Test overheat","type":"THRESHOLD","metric":"ENGINE_TEMP",
                 "operator":"GT","threshold":120,"severity":"CRITICAL","enabled":true}
                """;
        String location = mvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        Long id = com.jayway.jsonpath.JsonPath.parse(location).read("$.id", Long.class);
        mvc.perform(delete("/api/rules/{id}", id)).andExpect(status().isNoContent());
    }

    @Test
    void actuatorHealthIsUp() throws Exception {
        mvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void prometheusRegistryExposesCustomMetrics() {
        assertThat(prometheusRegistry.scrape()).contains("fleet_devices_online");
    }
}
