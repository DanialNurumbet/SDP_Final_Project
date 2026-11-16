package com.sportrent.pricing;

import com.sportrent.catalog.EquipmentFactory;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.ExtraLine;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.RentalTerms;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.sportrent.shared.model.EquipmentType.BICYCLE;
import static com.sportrent.shared.model.ExtraService.ACCESSORY_KIT;
import static com.sportrent.shared.model.ExtraService.INSURANCE;
import static com.sportrent.shared.model.Tariff.LONG_TERM;
import static com.sportrent.shared.model.Tariff.STANDARD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RentalPricingServiceTest {
    private final RentalPricingService service = new RentalPricingService();
    private final Equipment equipment = new EquipmentFactory().create(UUID.randomUUID(), BICYCLE, "Велосипед", new BigDecimal("1000.00"));

    @Test
    void standardKeepsFullBaseCost() {
        PriceBreakdown price = service.calculate(equipment, new RentalTerms(7, STANDARD, List.of()));
        BigDecimal base = new BigDecimal("7000.00");
        assertEquals(new PriceBreakdown(base, new BigDecimal("0.00"), base, List.of(), base), price);
    }

    @ParameterizedTest
    @CsvSource({"6,0.00,6000.00", "7,700.00,6300.00"})
    void longTermDiscountStartsAtSevenDays(int days, BigDecimal discount, BigDecimal total) {
        PriceBreakdown price = service.calculate(equipment, new RentalTerms(days, LONG_TERM, List.of()));
        assertEquals(discount, price.discount());
        assertEquals(total, price.total());
    }

    @Test
    void combinesExtrasAfterDiscount() {
        PriceBreakdown price = service.calculate(equipment,
                new RentalTerms(7, LONG_TERM, List.of(INSURANCE, ACCESSORY_KIT)));
        assertEquals(new PriceBreakdown(new BigDecimal("7000.00"), new BigDecimal("700.00"),
                new BigDecimal("6300.00"), List.of(new ExtraLine(INSURANCE, new BigDecimal("1400.00")),
                new ExtraLine(ACCESSORY_KIT, new BigDecimal("300.00"))), new BigDecimal("8000.00")), price);
    }

    @Test
    void roundsHalfUpToCents() {
        Equipment item = new EquipmentFactory().create(UUID.randomUUID(), BICYCLE, "Велосипед", new BigDecimal("1000.05"));
        PriceBreakdown price = service.calculate(item, new RentalTerms(7, LONG_TERM, List.of()));
        assertEquals(new BigDecimal("700.04"), price.discount());
        assertEquals(new BigDecimal("6300.31"), price.total());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 31})
    void rejectsInvalidDays(int days) {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.calculate(equipment, new RentalTerms(days, STANDARD, List.of())));
        assertEquals(400, error.getStatus());
    }

    @Test
    void rejectsDuplicateExtras() {
        ValidationException error = assertThrows(ValidationException.class,
                () -> service.calculate(equipment, new RentalTerms(7, LONG_TERM, List.of(INSURANCE, INSURANCE))));
        assertEquals(400, error.getStatus());
    }
}
