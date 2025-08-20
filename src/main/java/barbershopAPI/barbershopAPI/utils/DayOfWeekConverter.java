package barbershopAPI.barbershopAPI.utils;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.DayOfWeek;

/** Converte DayOfWeek (MON..SUN) <-> inteiro 1..7 para a coluna day_of_week */
@Converter(autoApply = false)
public class DayOfWeekConverter implements AttributeConverter<DayOfWeek, Integer> {
    @Override public Integer convertToDatabaseColumn(DayOfWeek attribute) {
        return attribute == null ? null : attribute.getValue(); // 1..7 (Mon..Sun)
    }
    @Override public DayOfWeek convertToEntityAttribute(Integer dbData) {
        return dbData == null ? null : DayOfWeek.of(dbData);
    }
}
