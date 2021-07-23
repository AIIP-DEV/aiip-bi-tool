package com.sk.bds.datainsight.util;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validation;
import java.util.Map;
import java.util.Set;

public class JsonUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        objectMapper.setPropertyNamingStrategy(propertyNamingStrategy);
    }

    public static <T> T unmarshal(String jsonText, Class<T> type) throws Exception {
        return objectMapper.readValue(jsonText, type);
    }

    public static String marshal(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    public static <T> T unmarshalWithValidation(String jsonText, Class<T> type) throws Exception {
        try {
            T t = objectMapper.readValue(jsonText, type);
            Set<ConstraintViolation<Object>> constraintViolations = Validation.buildDefaultValidatorFactory().getValidator().validate(t);
            if (!constraintViolations.isEmpty()) {
                throw new ConstraintViolationException(constraintViolations);
            }
            return t;
        } catch (JsonParseException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }

    public static Map objectToMap(Object object) {
        return objectMapper.convertValue(object, Map.class);
    }
}