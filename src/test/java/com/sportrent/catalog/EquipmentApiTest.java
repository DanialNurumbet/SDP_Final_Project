package com.sportrent.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.contract.RentalRepository;
import com.sportrent.storage.RentalFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import java.nio.file.Path;
import java.util.UUID;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EquipmentApiTest {
    @TempDir static Path directory;
    @DynamicPropertySource static void storagePath(DynamicPropertyRegistry registry) {
        registry.add("sportrent.storage.path", () -> directory.resolve("sportrent.json").toString());
    }
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired EquipmentRepository equipment;
    @Autowired RentalRepository rentals;

    private String createItem() throws Exception {
        String json = mvc.perform(post("/api/equipment").contentType("application/json")
                        .content("{\"type\":\"BALL\",\"name\":\"  Новый мяч  \",\"dailyRate\":123.455}"))
                .andExpect(status().isCreated()).andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Новый мяч"))
                .andExpect(jsonPath("$.dailyRate").value(123.46))
                .andExpect(jsonPath("$.usageHint").isNotEmpty())
                .andExpect(jsonPath("$.available").value(true))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json).get("id").asText();
    }

    @Test void createsReadsUpdatesAndDeletesEquipment() throws Exception {
        String id = createItem();
        mvc.perform(get("/api/equipment/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.type").value("BALL"));
        mvc.perform(put("/api/equipment/" + id).contentType("application/json")
                        .content("{\"type\":\"BALL\",\"name\":\"Мяч после изменения\",\"dailyRate\":500.00}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.dailyRate").value(500.00));
        mvc.perform(delete("/api/equipment/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/equipment/" + id)).andExpect(status().isNotFound());
    }

    @Test void filtersAndProtectsOccupiedItems() throws Exception {
        String id = createItem();
        rentals.save(RentalFixtures.active(equipment.findById(UUID.fromString(id)).orElseThrow()));
        mvc.perform(get("/api/equipment").param("type", "BALL").param("available", "false"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[*].id", hasItem(id)))
                .andExpect(jsonPath("$[*].type", everyItem(is("BALL"))))
                .andExpect(jsonPath("$[*].available", everyItem(is(false))));
        mvc.perform(delete("/api/equipment/" + id)).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EQUIPMENT_IN_USE"));
        mvc.perform(put("/api/equipment/" + id).contentType("application/json")
                        .content("{\"type\":\"BALL\",\"name\":\"Новый мяч\",\"dailyRate\":500}"))
                .andExpect(status().isConflict());
    }

    @Test void forbidsChangingType() throws Exception {
        String id = createItem();
        mvc.perform(put("/api/equipment/" + id).contentType("application/json")
                        .content("{\"type\":\"RACKET\",\"name\":\"Новая ракетка\",\"dailyRate\":500}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.type").isNotEmpty());
    }

    @Test void returnsFieldErrorsForInvalidValues() throws Exception {
        mvc.perform(post("/api/equipment").contentType("application/json")
                        .content("{\"type\":\"BALL\",\"name\":\" \",\"dailyRate\":0}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.name").isNotEmpty())
                .andExpect(jsonPath("$.fieldErrors.dailyRate").isNotEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "null", "{broken", "{\"type\":\"UNKNOWN\",\"name\":\"Мяч\",\"dailyRate\":50}",
            "{\"type\":1,\"name\":\"Мяч\",\"dailyRate\":50}",
            "{\"type\":\"BALL\",\"name\":123,\"dailyRate\":50}",
            "{\"type\":\"BALL\",\"name\":\"Мяч\",\"dailyRate\":\"50\"}",
            "{\"type\":\"BALL\",\"name\":\"Мяч\",\"dailyRate\":50} {}",
            "{\"type\":\"BALL\",\"name\":\"Мяч\",\"dailyRate\":\"NaN\"}"})
    void rejectsMalformedRequests(String body) throws Exception {
        mvc.perform(post("/api/equipment").contentType("application/json").content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").isNotEmpty());
    }

    @Test void validatesIdsAndFilters() throws Exception {
        mvc.perform(get("/api/equipment/not-a-uuid")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/equipment/" + UUID.randomUUID())).andExpect(status().isNotFound());
        mvc.perform(get("/api/equipment").param("type", "UNKNOWN")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/equipment").param("available", "maybe")).andExpect(status().isBadRequest());
    }
}
