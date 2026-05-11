package com.thiagsilvadev.erpapi.common.format;

public final class ContactFormatter {

    private ContactFormatter() {}

    public static String formatBrazilianPhone(String phone) {
        if (phone == null) {
            return null;
        }

        if (phone.length() == 11) {
            return phone.replaceFirst("(\\d{2})(\\d{5})(\\d{4})", "($1) $2-$3");
        }

        return phone.replaceFirst("(\\d{2})(\\d{4})(\\d{4})", "($1) $2-$3");
    }
}
