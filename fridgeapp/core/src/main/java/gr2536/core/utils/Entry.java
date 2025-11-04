package gr2536.core.utils;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

public final class Entry {

    public final String displayName;

    public final Map<LocalDate, Integer> quantitiesByExpiration =
            new TreeMap<>(Comparator.nullsLast(Comparator.naturalOrder()));

    public Entry(String displayName) {
        this.displayName = displayName;
    }
}

