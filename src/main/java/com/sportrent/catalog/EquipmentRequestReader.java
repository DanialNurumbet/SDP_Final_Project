package com.sportrent.catalog;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.sportrent.shared.dto.EquipmentRequest;
import com.sportrent.shared.error.ValidationException;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

@Component
public class EquipmentRequestReader {
    private final ObjectReader reader;

    public EquipmentRequestReader(ObjectMapper sharedMapper) {
        // Строгий разбор только каталога; настройки других модулей не меняются.
        ObjectMapper mapper = sharedMapper.copy()
                .enable(DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS)
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        for (CoercionInputShape shape : new CoercionInputShape[]{CoercionInputShape.Integer, CoercionInputShape.Float, CoercionInputShape.Boolean}) {
            mapper.coercionConfigFor(LogicalType.Textual).setCoercion(shape, CoercionAction.Fail);
        }
        mapper.coercionConfigFor(BigDecimal.class).setCoercion(CoercionInputShape.String, CoercionAction.Fail);
        reader = mapper.readerFor(EquipmentRequest.class);
    }

    public EquipmentRequest read(String json) {
        try {
            EquipmentRequest request = reader.readValue(json);
            if (request == null) throw new ValidationException("Укажите данные предмета.");
            return request;
        } catch (JsonProcessingException error) {
            throw new ValidationException("Некорректные данные: укажите строковые тип и название, числовую цену.");
        }
    }
}
