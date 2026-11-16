package com.sportrent.reporting;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.contract.RentalRepository;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.Rental;
import com.sportrent.storage.RentalFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportingApiTest {

    @TempDir
    static Path directory;

    @DynamicPropertySource
    static void storagePath(DynamicPropertyRegistry registry) {
        registry.add("sportrent.storage.path", () -> directory.resolve("sportrent.json").toString());
    }

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    EquipmentRepository equipment;
    @Autowired
    RentalRepository rentals;

    private JsonNode summary() throws Exception {
        String json = mvc.perform(get("/api/reports/summary")).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(json);
    }

    @Test
    void savedActiveRentalAppearsInHistoryDetailsAndSummary() throws Exception {
        JsonNode before = summary();
        Equipment item = equipment.findAll().get(0);
        Rental rental = rentals.save(RentalFixtures.active(item));
        String id = rental.id().toString();

        mvc.perform(get("/api/rentals")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mvc.perform(get("/api/rentals").param("status", "ACTIVE")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(id)));
        mvc.perform(get("/api/rentals").param("status", "COMPLETED")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(id))));
        mvc.perform(get("/api/rentals/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.customer.name").value("Тестовый клиент"))
                .andExpect(jsonPath("$.equipmentSnapshot.name").value(item.getName()))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        JsonNode after = summary();
        assertEquals(before.get("totalCount").asInt() + 1, after.get("totalCount").asInt());
        assertEquals(before.get("activeCount").asInt() + 1, after.get("activeCount").asInt());
        assertEquals(before.get("completedCount").asInt(), after.get("completedCount").asInt());
        assertEquals(before.get("totalRentalsAmount").asDouble() + item.getDailyRate().doubleValue(),
                after.get("totalRentalsAmount").asDouble(), 0.001);
    }

    @Test
    void unknownOrMalformedRequestsReturnUnifiedErrors() throws Exception {
        mvc.perform(get("/api/rentals/" + UUID.randomUUID())).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RENTAL_NOT_FOUND"));
        mvc.perform(get("/api/rentals/not-a-uuid")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
        mvc.perform(get("/api/rentals").param("status", "FINISHED")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"));
    }

    @Test
    void textReportIsDownloadableUtf8Text() throws Exception {
        rentals.save(RentalFixtures.active(equipment.findAll().get(1)));

        mvc.perform(get("/api/reports/rentals.txt")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().string(containsString("ОТЧЁТ ОБ АРЕНДАХ SPORTRENT")))
                .andExpect(content().string(containsString("Клиент: Тестовый клиент")));
    }
}
