package org.threeten.bp.jdk7;

import java.util.Comparator;

public class Jdk7Methods {

    private Jdk7Methods() {
        super();
    }

    public static int Long_compare(long x, long y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static int Integer_compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    public static boolean Objects_equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static <T> int Objects_compare(T a, T b, Comparator<? super T> c) {
        return (a == b) ? 0 : c.compare(a, b);
    }

    public static <T> T Objects_requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException();
        return obj;
    }

    public static <T> T Objects_requireNonNull(T obj, String message) {
        if (obj == null)
            throw new NullPointerException(message);
        return obj;
    }

}
