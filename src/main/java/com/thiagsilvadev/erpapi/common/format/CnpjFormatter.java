package com.thiagsilvadev.erpapi.common.format;

public final class CnpjFormatter {

    private CnpjFormatter() {}

    public static String format(String cnpj) {
        return cnpj.replaceFirst("(\\d{2})(\\d{3})(\\d{3})(\\d{4})(\\d{2})", "$1.$2.$3/$4-$5");
    }
}
