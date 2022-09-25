package com.shade.decima.ui.data.managers;

import com.shade.decima.ui.data.ValueController;
import com.shade.decima.ui.data.ValueEditor;
import com.shade.decima.ui.data.ValueManager;
import com.shade.decima.ui.data.ValueManagerRegistration;
import com.shade.decima.ui.data.editors.BoolValueEditor;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

@ValueManagerRegistration(names = "bool")
public class BoolValueManager implements ValueManager<Boolean> {
    @Nullable
    @Override
    public ValueEditor<Boolean> createEditor(@NotNull ValueController<Boolean> controller) {
        return switch (controller.getEditType()) {
            case INLINE -> new BoolValueEditor(controller);
            case PANEL -> null;
        };
    }
}