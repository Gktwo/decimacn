package com.shade.decima.ui.handlers;

import com.shade.decima.rtti.RTTIType;
import com.shade.decima.rtti.types.RTTITypeArray;
import com.shade.decima.rtti.types.RTTITypeClass;
import com.shade.decima.rtti.types.RTTITypeHashMap;
import com.shade.decima.ui.handlers.impl.*;
import com.shade.decima.util.NotNull;

public final class ValueHandlerProvider {
    private ValueHandlerProvider() {
    }

    @NotNull
    public static ValueHandler getValueHandler(@NotNull RTTIType<?> type) {
        if (type.getName().equals("GGUUID")) {
            return GGUUIDValueHandler.INSTANCE;
        } else if (type instanceof RTTITypeClass) {
            return ObjectValueHandler.INSTANCE;
        } else if (type instanceof RTTITypeArray) {
            return ArrayValueHandler.INSTANCE;
        } else if (type instanceof RTTITypeHashMap) {
            return HashMapValueHandler.INSTANCE;
        } else if (type instanceof RTTITypeString) {
            return StringValueHandler.INSTANCE;
        } else {
            return DefaultValueHandler.INSTANCE;
        }
    }
}