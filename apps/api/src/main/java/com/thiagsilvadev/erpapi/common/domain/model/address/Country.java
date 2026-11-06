package com.thiagsilvadev.erpapi.common.domain.model.address;

public enum Country {
    BR(1058, "Brazil"),
    US(2496, "United States"),
    CN(163, "China"),
    AR(639, "Argentina"),
    PY(5860, "Paraguay"),
    UY(8451, "Uruguay"),
    DE(245, "Germany"),
    PT(6760, "Portugal"),
    GB(3280, "United Kingdom");

    private final int bacenCode;
    private final String name;

    Country(int bacenCode, String name) {
        this.bacenCode = bacenCode;
        this.name = name;
    }

    public int getBacenCode() {
        return bacenCode;
    }

    public String getName() {
        return name;
    }
}
