package vn.edu.iuh.fit.backend.converters;

import com.neovisionaries.i18n.CountryCode;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CountryCodeConverter implements AttributeConverter<CountryCode, Short> {

    @Override
    public Short convertToDatabaseColumn(CountryCode countryCode) {
        if (countryCode == null) {
            return null;
        }
        // Lấy mã numeric của quốc gia
        return (short) countryCode.getNumeric();
    }

    @Override
    public CountryCode convertToEntityAttribute(Short dbData) {
        if (dbData == null) {
            return null;
        }
        // Lấy CountryCode từ mã numeric
        for (CountryCode countryCode : CountryCode.values()) {
            if (countryCode.getNumeric() == dbData) {
                return countryCode;
            }
        }
        return null; // Trả về null nếu không tìm thấy
    }
}
