package com.gestortareas.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoRecordatorio {
    DIA_ANTES, HORA_ANTES, PERSONALIZADO;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static TipoRecordatorio fromJson(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }
}
