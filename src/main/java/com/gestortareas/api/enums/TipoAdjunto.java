package com.gestortareas.api.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TipoAdjunto {
    ARCHIVO, ENLACE;

    @JsonValue
    public String toJson() {
        return name().toLowerCase();
    }

    @JsonCreator
    public static TipoAdjunto fromJson(String value) {
        if (value == null) return null;
        return valueOf(value.toUpperCase());
    }
}
