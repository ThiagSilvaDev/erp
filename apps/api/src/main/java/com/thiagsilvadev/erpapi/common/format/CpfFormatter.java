package com.thiagsilvadev.erpapi.common.format;

public final class CpfFormatter {

    private CpfFormatter() {}

    public static String format(String cpf) {
        return cpf.replaceFirst("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.$2.$3-$4");
    }
}
