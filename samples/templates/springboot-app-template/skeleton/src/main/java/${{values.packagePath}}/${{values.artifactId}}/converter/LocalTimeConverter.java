package ${{ values.packageName }}.${{ values.artifactId }}.converter;

import javax.persistence.AttributeConverter;
import java.sql.Time;
import java.time.LocalTime;

public class LocalTimeConverter implements AttributeConverter <LocalTime, Time> {

    @Override
    public Time convertToDatabaseColumn(LocalTime attribute) {
        return attribute != null ? Time.valueOf(attribute) : null;
    }

    @Override
    public LocalTime convertToEntityAttribute(Time dbData) {
        return dbData != null ? dbData.toLocalTime() : null;
    }

}