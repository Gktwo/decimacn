package com.shade.decima.ui.editor.core;

import com.shade.decima.model.rtti.objects.RTTIObject;
import com.shade.decima.model.rtti.path.PathElementEntry;
import com.shade.decima.model.rtti.registry.RTTITypeRegistry;
import com.shade.util.NotNull;

public class CoreNodeEntry extends CoreNodeObject {
    private final int index;

    public CoreNodeEntry(@NotNull CoreNodeBinary parent, @NotNull RTTIObject object, int index) {
        super(parent, object.type(), RTTITypeRegistry.getFullTypeName(object.type()), new PathElementEntry(object));
        this.index = index;
    }

    @NotNull
    public RTTIObject getObjectUUID() {
        return ((RTTIObject) getValue()).get("ObjectUUID");
    }

    public int getIndex() {
        return index;
    }
}
