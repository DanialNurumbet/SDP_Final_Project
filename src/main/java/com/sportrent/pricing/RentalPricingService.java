package com.sportrent.pricing;

import com.sportrent.shared.contract.PricingService;
import com.sportrent.shared.error.ValidationException;
import com.sportrent.shared.model.Equipment;
import com.sportrent.shared.model.ExtraService;
import com.sportrent.shared.model.PriceBreakdown;
import com.sportrent.shared.model.RentalTerms;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class RentalPricingService implements PricingService {
    private final PricingStrategy standard = new StandardPricingStrategy();
    private final PricingStrategy longTerm = new LongTermPricingStrategy();

    @Override
    public PriceBreakdown calculate(Equipment equipment, RentalTerms terms) {
        if (terms.days() < 1 || terms.days() > 30) {
            throw new ValidationException("Укажите срок аренды от 1 до 30 дней.",
                    Map.of("days", "Допустимый срок — от 1 до 30 дней."));
        }
        if (terms.tariff() == null) {
            throw new ValidationException("Выберите тариф.", Map.of("tariff", "Тариф обязателен."));
        }

        PricingStrategy strategy = switch (terms.tariff()) {
            case STANDARD -> standard;
            case LONG_TERM -> longTerm;
        };
        RentalPrice price = new BaseRentalPrice(strategy);
        List<ExtraService> extras = terms.extras() == null ? List.of() : terms.extras();
        EnumSet<ExtraService> seen = EnumSet.noneOf(ExtraService.class);
        for (ExtraService extra : extras) {
            if (extra == null || !seen.add(extra)) {
                throw new ValidationException("Каждую услугу можно выбрать только один раз.",
                        Map.of("extras", "Укажите известные услуги без повторов."));
            }
            price = switch (extra) {
                case INSURANCE -> new InsuranceDecorator(price);
                case ACCESSORY_KIT -> new AccessoryKitDecorator(price);
            };
        }
        return price.calculate(equipment.getDailyRate(), terms.days());
    }
}
