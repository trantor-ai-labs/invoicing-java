package com.acme.invoicing.support;

import java.lang.reflect.Field;
import java.util.ArrayList;

public final class ListDiagnostics {
    private ListDiagnostics() {
    }

    public static int capacityOf(ArrayList<?> list) {
        try {
            Field backing = ArrayList.class.getDeclaredField("elementData");
            backing.setAccessible(true);
            return ((Object[]) backing.get(list)).length;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException("Cannot probe list capacity", e);
        }
    }

    public static String describe(ArrayList<?> list) {
        return "size=" + list.size() + " capacity=" + capacityOf(list);
    }
}
