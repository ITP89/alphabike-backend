package com.alphabike.backend.shared.validation;

import com.alphabike.backend.shared.exception.BadRequestException;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public final class EnumUtils {

    private EnumUtils() {
    }

    public static <E extends Enum<E>> E parse(Class<E> enumClass, String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(fieldName + " es obligatorio");
        }

        try {
            return Enum.valueOf(enumClass, value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(fieldName + " invalido. Valores permitidos: " + allowedValues(enumClass));
        }
    }

    private static <E extends Enum<E>> String allowedValues(Class<E> enumClass) {
        return Arrays.stream(enumClass.getEnumConstants())
                .map(Enum::name)
                .collect(Collectors.joining(", "));
    }
}
