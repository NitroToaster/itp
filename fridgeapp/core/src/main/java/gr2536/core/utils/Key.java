package gr2536.core.utils;

import java.util.Locale;

public record Key(String name) {

    public static Key of(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must be non-empty");
        }
        return new Key(name.trim().toLowerCase(Locale.ROOT));
    }
}
