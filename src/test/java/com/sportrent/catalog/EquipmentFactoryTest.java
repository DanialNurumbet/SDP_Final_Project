package com.sportrent.catalog;

import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.EquipmentType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentFactoryTest {
    private final EquipmentFactory factory = new EquipmentFactory();

    @Test
    void createsConcreteProductsWithDifferentHints() {
        UUID id = UUID.randomUUID();
        Equipment bicycle = factory.create(id, EquipmentType.BICYCLE, " Велосипед №1 ", new BigDecimal("1000.125"));
        Equipment ball = factory.create(UUID.randomUUID(), EquipmentType.BALL, "Мяч №1", BigDecimal.TEN);
        Equipment racket = factory.create(UUID.randomUUID(), EquipmentType.RACKET, "Ракетка №1", BigDecimal.TEN);
        assertInstanceOf(Bicycle.class, bicycle);
        assertInstanceOf(Ball.class, ball);
        assertInstanceOf(Racket.class, racket);
        assertEquals(id, bicycle.getId());
        assertEquals("Велосипед №1", bicycle.getName());
        assertEquals(new BigDecimal("1000.13"), bicycle.getDailyRate());
        assertEquals(3, java.util.Set.of(bicycle.getUsageHint(), ball.getUsageHint(), racket.getUsageHint()).size());
    }

    @Test
    void rejectsInvalidNamesRatesAndMissingType() {
        UUID id = UUID.randomUUID();
        for (String name : new String[]{"", " ", "Я", "x".repeat(101)}) {
            assertThrows(ValidationException.class, () -> factory.create(id, EquipmentType.BALL, name, BigDecimal.TEN));
        }
        for (BigDecimal rate : new BigDecimal[]{null, BigDecimal.ZERO, new BigDecimal("-1"), new BigDecimal("0.009"), new BigDecimal("1000000.01")}) {
            assertThrows(ValidationException.class, () -> factory.create(id, EquipmentType.BALL, "Мяч", rate));
        }
        assertThrows(ValidationException.class, () -> factory.create(id, null, "Мяч", BigDecimal.TEN));
    }
}

