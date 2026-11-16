package com.sportrent.pricing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.sportrent.shared.contract.EquipmentRepository;
import com.sportrent.shared.contract.PricingService;
import com.sportrent.shared.dto.QuoteRequest;
import com.sportrent.shared.error.NotFoundException;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.RentalTerms;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PricingController {
    private final EquipmentRepository equipmentRepository;
    private final PricingService pricingService;
    private final ObjectReader quoteReader;

    public PricingController(EquipmentRepository equipmentRepository, PricingService pricingService,
                             ObjectMapper sharedMapper) {
        this.equipmentRepository = equipmentRepository;
        this.pricingService = pricingService;
        ObjectMapper mapper = sharedMapper.copy()
                .disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT)
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        mapper.coercionConfigFor(Integer.class).setCoercion(CoercionInputShape.String, CoercionAction.Fail);
        this.quoteReader = mapper.readerFor(QuoteRequest.class);
    }

    @PostMapping(path = "/api/pricing/quote", consumes = MediaType.APPLICATION_JSON_VALUE)
    public PriceBreakdown quote(@RequestBody String json) {
        QuoteRequest request = readQuote(json);
        if (request == null || request.equipmentId() == null) {
            throw new ValidationException("Выберите предмет.", Map.of("equipmentId", "Предмет обязателен."));
        }
        if (request.days() == null) {
            throw new ValidationException("Укажите срок аренды.", Map.of("days", "Срок обязателен."));
        }
        Equipment equipment = equipmentRepository.findById(request.equipmentId())
                .orElseThrow(() -> new NotFoundException("EQUIPMENT_NOT_FOUND", "Предмет не найден."));
        return pricingService.calculate(equipment, new RentalTerms(request.days(), request.tariff(), request.extras()));
    }

    private QuoteRequest readQuote(String json) {
        try {
            return quoteReader.readValue(json);
        } catch (JsonProcessingException exception) {
            throw new ValidationException("Проверьте UUID предмета, целое число дней, тариф и список услуг.");
        }
    }
}
