package com.shade.decima.ui.editor.binary;

import com.shade.decima.ui.editor.FileEditorInput;
import com.shade.platform.ui.editors.Editor;
import com.shade.platform.ui.editors.EditorInput;
import com.shade.platform.ui.editors.EditorProvider;
import com.shade.util.NotNull;
import com.shade.util.Nullable;

import javax.swing.*;

public class BinaryEditorProvider implements EditorProvider {
    @NotNull
    @Override
    public Editor createEditor(@NotNull EditorInput input) {
        return new BinaryEditor((FileEditorInput) input);
    }

    @Override
    public boolean supports(@NotNull EditorInput input) {
        return input instanceof FileEditorInput;
    }

    @NotNull
    @Override
    public String getName() {
        return "�����Ʊ༭��";
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return UIManager.getIcon("Navigator.binaryIcon");
    }
}
