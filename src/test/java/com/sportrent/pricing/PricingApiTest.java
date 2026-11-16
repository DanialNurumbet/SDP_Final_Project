package com.sportrent.pricing;

import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.model.Equipment;
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

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.UUID;

import static org.hamcrest.Matchers.comparesEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PricingApiTest {
    @TempDir
    static Path directory;

    @DynamicPropertySource
    static void storagePath(DynamicPropertyRegistry registry) {
        registry.add("sportrent.storage.path", () -> directory.resolve("sportrent.json").toString());
    }

    @Autowired
    MockMvc mvc;

    @Autowired
    EquipmentRepository equipmentRepository;

    @Test
    void quotesCatalogRateAndBothExtras() throws Exception {
        Equipment item = equipmentRepository.findAll().getFirst();
        mvc.perform(post("/api/pricing/quote").contentType(APPLICATION_JSON)
                        .content(request(item.getId(), "7", "\"STANDARD\"", "[\"INSURANCE\",\"ACCESSORY_KIT\"]")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.baseCost").value(comparesEqualTo(item.getDailyRate().multiply(BigDecimal.valueOf(7))), BigDecimal.class))
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.extras[0].cost").value(1400))
                .andExpect(jsonPath("$.extras[1].cost").value(300));
    }

    @ParameterizedTest
    @ValueSource(strings = {"7.5", "7.0", "\"7\"", "0", "31"})
    void rejectsNonIntegerOrOutOfRangeDays(String days) throws Exception {
        UUID id = equipmentRepository.findAll().getFirst().getId();
        mvc.perform(post("/api/pricing/quote").contentType(APPLICATION_JSON)
                        .content(request(id, days, "\"STANDARD\"", "[]")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnknownTariffAndDuplicateExtras() throws Exception {
        UUID id = equipmentRepository.findAll().getFirst().getId();
        mvc.perform(post("/api/pricing/quote").contentType(APPLICATION_JSON)
                        .content(request(id, "7", "0", "[]")))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/pricing/quote").contentType(APPLICATION_JSON)
                        .content(request(id, "7", "\"STANDARD\"", "[\"INSURANCE\",\"INSURANCE\"]")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.extras").exists());
    }

    @Test
    void returnsNotFoundForUnknownEquipment() throws Exception {
        mvc.perform(post("/api/pricing/quote").contentType(APPLICATION_JSON)
                        .content(request(UUID.randomUUID(), "7", "\"STANDARD\"", "[]")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EQUIPMENT_NOT_FOUND"));
    }

    private static String request(UUID equipmentId, String days, String tariff, String extras) {
        return """
                {"equipmentId":"%s","days":%s,"tariff":%s,"extras":%s}
                """.formatted(equipmentId, days, tariff, extras);
    }
}
