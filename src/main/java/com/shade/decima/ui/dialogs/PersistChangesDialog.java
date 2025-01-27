package com.shade.decima.ui.dialogs;

import com.shade.decima.model.base.GameType;
import com.shade.decima.model.packfile.Packfile;
import com.shade.decima.model.packfile.PackfileBase;
import com.shade.decima.model.packfile.PackfileWriter;
import com.shade.decima.model.packfile.edit.Change;
import com.shade.decima.model.packfile.resource.PackfileResource;
import com.shade.decima.model.util.Compressor;
import com.shade.decima.ui.Application;
import com.shade.decima.ui.controls.FileExtensionFilter;
import com.shade.decima.ui.controls.LabeledBorder;
import com.shade.decima.ui.navigator.NavigatorTree;
import com.shade.decima.ui.navigator.impl.FilePath;
import com.shade.decima.ui.navigator.impl.NavigatorFileNode;
import com.shade.decima.ui.navigator.impl.NavigatorFolderNode;
import com.shade.decima.ui.navigator.impl.NavigatorProjectNode;
import com.shade.platform.model.runtime.ProgressMonitor;
import com.shade.platform.ui.controls.ColoredListCellRenderer;
import com.shade.platform.ui.controls.CommonTextAttributes;
import com.shade.platform.ui.controls.Mnemonic;
import com.shade.platform.ui.controls.TextAttributes;
import com.shade.platform.ui.dialogs.BaseDialog;
import com.shade.platform.ui.dialogs.ProgressDialog;
import com.shade.platform.ui.util.UIUtils;
import com.shade.util.NotNull;
import com.shade.util.Nullable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.nio.file.StandardOpenOption.*;

public class PersistChangesDialog extends BaseDialog {
    private static final CompressionLevel[] COMPRESSION_LEVELS = {
        new CompressionLevel(Compressor.Level.NONE, "无", "不压缩"),
        new CompressionLevel(Compressor.Level.SUPER_FAST, "超快", "超快模式，较低压缩比"),
        new CompressionLevel(Compressor.Level.VERY_FAST, "非常快", "最快模式，压缩比适中"),
        new CompressionLevel(Compressor.Level.FAST, "快", "适合日常使用"),
        new CompressionLevel(Compressor.Level.NORMAL, "正常的", "标准中速模式"),
        new CompressionLevel(Compressor.Level.OPTIMAL_1, "最优的", "更快的优化压缩"),
        new CompressionLevel(Compressor.Level.OPTIMAL_2, "最优的 2", "推荐的基线最佳编码器"),
        new CompressionLevel(Compressor.Level.OPTIMAL_3, "最优的 3", "较慢的最佳编码器"),
        new CompressionLevel(Compressor.Level.OPTIMAL_4, "最优的 4", "非常慢的最佳编码器"),
        new CompressionLevel(Compressor.Level.OPTIMAL_5, "最优的 5", "最大压缩，非常慢")
    };

    private static final PackfileType[] PACKFILE_TYPES = {
        new PackfileType("Regular", EnumSet.allOf(GameType.class)),
        new PackfileType("Encrypted", EnumSet.of(GameType.DS)),
    };

    private final NavigatorProjectNode root;
    private final JRadioButton updateExistingPackfileButton;
    private final JRadioButton createPatchPackfileButton;
    private final JCheckBox createBackupCheckbox;
    private final JCheckBox appendIfExistsCheckbox;
    private final JComboBox<CompressionLevel> compressionLevelCombo;
    private final JComboBox<PackfileType> packfileTypeCombo;

    public PersistChangesDialog(@NotNull NavigatorProjectNode root) {
        super("保持更改", List.of(BUTTON_PERSIST, BUTTON_CANCEL));

        this.root = root;

        this.updateExistingPackfileButton = Mnemonic.resolve(new JRadioButton("更新已更改的包文件", null, false));
        this.updateExistingPackfileButton.setToolTipText("仅重新打包其文件已更改的包文件。\nBig打包文件可能需要很长时间才能重新打包.");

        this.createPatchPackfileButton = Mnemonic.resolve(new JRadioButton("将更改收集到单个数据包文件中", null, true));
        this.createPatchPackfileButton.setToolTipText("创建一个包含修改后的包文件的所有更改的单个包文件。\n在不同的数据包文件之间更改同一文件时，不能使用此选项.");

        this.createBackupCheckbox = Mnemonic.resolve(new JCheckBox("创建备份（如果存在）", true));
        this.createBackupCheckbox.setToolTipText("为每个修改的数据包文件创建备份，以便以后可以恢复.");

        this.appendIfExistsCheckbox = Mnemonic.resolve(new JCheckBox("如果存在则追加", true));
        this.appendIfExistsCheckbox.setToolTipText("如果选定的数据包文件存在，则附加更改而不是截断更改.");

        this.compressionLevelCombo = new JComboBox<>(COMPRESSION_LEVELS);
        this.compressionLevelCombo.setSelectedItem(COMPRESSION_LEVELS[3]);
        this.compressionLevelCombo.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends CompressionLevel> list, @NotNull CompressionLevel value, int index, boolean selected, boolean focused) {
                append(value.name(), TextAttributes.REGULAR_ATTRIBUTES);

                if (value.description() != null) {
                    append(" " + value.description(), TextAttributes.GRAYED_SMALL_ATTRIBUTES);
                }
            }
        });

        this.packfileTypeCombo = new JComboBox<>(PACKFILE_TYPES);
        this.packfileTypeCombo.setSelectedItem(PACKFILE_TYPES[0]);
        this.packfileTypeCombo.setRenderer(new ColoredListCellRenderer<>() {
            @Override
            protected void customizeCellRenderer(@NotNull JList<? extends PackfileType> list, @NotNull PackfileType value, int index, boolean selected, boolean focused) {
                append(value.name(), TextAttributes.REGULAR_ATTRIBUTES);

                if (!value.games().contains(root.getProjectContainer().getType())) {
                    append(" 不兼容与 " + root.getProjectContainer().getType(), CommonTextAttributes.IDENTIFIER_ATTRIBUTES.smaller());
                }
            }
        });

        final ButtonGroup group = new ButtonGroup();
        group.add(updateExistingPackfileButton);
        group.add(createPatchPackfileButton);

        final boolean canMergeChanges = root.getProject().getPackfileManager().canMergeChanges();
        updateExistingPackfileButton.setSelected(!canMergeChanges);
        createPatchPackfileButton.setEnabled(canMergeChanges);
        createPatchPackfileButton.addItemListener(e -> appendIfExistsCheckbox.setEnabled(createPatchPackfileButton.isSelected()));
    }

    @NotNull
    @Override
    protected JComponent createContentsPane() {
        final JPanel settings = new JPanel();
        settings.setLayout(new MigLayout("ins panel", "[fill][grow,fill,250lp]", ""));
        settings.setBorder(new LabeledBorder(new JLabel("Settings")));

        {
            final JPanel top = new JPanel();
            top.setLayout(new MigLayout("ins 0", "[fill][fill]", ""));

            top.add(new JLabel("Strategy:"), "cell 0 0");
            top.add(updateExistingPackfileButton, "cell 0 1");
            top.add(createPatchPackfileButton, "cell 0 2");

            top.add(new JLabel("Options:"), "cell 1 0");
            top.add(createBackupCheckbox, "cell 1 1");
            top.add(appendIfExistsCheckbox, "cell 1 2");

            settings.add(top, "span");
        }

        final JLabel packfileTypeLabel = Mnemonic.resolve(new JLabel("Archive &format:"));
        packfileTypeLabel.setLabelFor(packfileTypeCombo);

        settings.add(packfileTypeLabel);
        settings.add(packfileTypeCombo, "wrap");

        final JLabel compressionLevelLabel = Mnemonic.resolve(new JLabel("Compression &level:"));
        compressionLevelLabel.setLabelFor(compressionLevelCombo);

        settings.add(compressionLevelLabel);
        settings.add(compressionLevelCombo, "wrap");

        final JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(createFilteredTree()), BorderLayout.CENTER);
        panel.add(settings, BorderLayout.SOUTH);

        return panel;
    }

    @Override
    protected void buttonPressed(@NotNull ButtonDescriptor descriptor) {
        if (descriptor == BUTTON_PERSIST) {
            final var update = updateExistingPackfileButton.isSelected();
            final var compression = compressionLevelCombo.getItemAt(compressionLevelCombo.getSelectedIndex()).level();
            final var encrypt = packfileTypeCombo.getItemAt(packfileTypeCombo.getSelectedIndex()) == PACKFILE_TYPES[1];
            final boolean success;

            try {
                success = persist(update, new PackfileWriter.Options(compression, encrypt));
            } catch (IOException e) {
                throw new RuntimeException("Error persisting changes", e);
            }

            if (!success) {
                return;
            }

            ProgressDialog.showProgressDialog(getDialog(), "Reload packfiles", monitor -> {
                for (Packfile packfile : root.getProject().getPackfileManager().getPackfiles()) {
                    if (packfile.hasChanges()) {
                        try {
                            packfile.reload();
                        } catch (IOException e) {
                            UIUtils.showErrorDialog(Application.getFrame(), e, "Unable to reload packfile");
                        }
                    }
                }
                return null;
            });

            if (update) {
                JOptionPane.showMessageDialog(getDialog(), "Packfiles were updated successfully.");
            } else {
                JOptionPane.showMessageDialog(getDialog(), "Patch packfile was created successfully.");
            }
        }

        super.buttonPressed(descriptor);
    }

    @Nullable
    @Override
    protected ButtonDescriptor getDefaultButton() {
        return BUTTON_PERSIST;
    }

    @NotNull
    private NavigatorTree createFilteredTree() {
        final NavigatorTree tree = new NavigatorTree(root);

        tree.getModel().setFilter(node -> {
            if (node instanceof NavigatorFolderNode n) {
                return n.getPackfile().hasChangesInPath(n.getPath());
            }
            if (node instanceof NavigatorFileNode n) {
                return n.getPackfile().hasChangesInPath(n.getPath());
            }
            return false;
        });

        tree.setRootVisible(false);

        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }

        return tree;
    }

    private boolean persist(boolean update, @NotNull PackfileWriter.Options options) throws IOException {
        if (update) {
            final int result = JOptionPane.showConfirmDialog(
                getDialog(),
                "Updating modified packfiles can take a significant amount of time and render the game unplayable if important files were changed.\n\nAdditionally, to see the changes in the application, you might need to reload the project.\n\nDo you want to continue?",
                "Confirm Update",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                ProgressDialog.showProgressDialog(getDialog(), "Persist changes", monitor -> {
                    updateExistingPackfiles(monitor, options, createBackupCheckbox.isSelected());
                    return null;
                });

                return true;
            }
        } else {
            final JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Choose output packfile");
            chooser.setFileFilter(new FileExtensionFilter("Decima packfile", "bin"));
            chooser.setAcceptAllFileFilterUsed(false);

            final int result = chooser.showSaveDialog(getDialog());

            if (result == JFileChooser.APPROVE_OPTION) {
                ProgressDialog.showProgressDialog(getDialog(), "Persist changes", monitor -> {
                    collectSinglePackfile(monitor, chooser.getSelectedFile().toPath(), options, appendIfExistsCheckbox.isSelected(), createBackupCheckbox.isSelected());
                    return null;
                });

                return true;
            }
        }

        return false;
    }

    private void collectSinglePackfile(@NotNull ProgressMonitor monitor, @NotNull Path path, @NotNull PackfileWriter.Options options, boolean append, boolean backup) throws IOException {
        final Packfile packfile;

        if (append && Files.exists(path)) {
            packfile = new Packfile(path, root.getProject().getCompressor(), null);
        } else {
            packfile = null;
        }

        final var project = root.getProject();
        final var manager = project.getPackfileManager();
        final var changes = manager.getPackfiles().stream()
            .filter(Packfile::hasChanges)
            .flatMap(p -> p.getChanges().entrySet().stream())
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        try (packfile) {
            write(monitor, path, packfile, options, changes, backup);
        }
    }

    private void updateExistingPackfiles(@NotNull ProgressMonitor monitor, @NotNull PackfileWriter.Options options, boolean backup) throws IOException {
        final var project = root.getProject();
        final var manager = project.getPackfileManager();
        final var changes = manager.getPackfiles().stream()
            .filter(Packfile::hasChanges)
            .collect(Collectors.toMap(
                Function.identity(),
                Packfile::getChanges
            ));

        try (ProgressMonitor.Task task = monitor.begin("Update packfiles", changes.size())) {
            for (var changesPerPackfile : changes.entrySet()) {
                write(monitor, changesPerPackfile.getKey().getPath(), changesPerPackfile.getKey(), options, changesPerPackfile.getValue(), backup);
                task.worked(1);
            }
        }
    }

    private void write(@NotNull ProgressMonitor monitor, @NotNull Path path, @Nullable Packfile target, @NotNull PackfileWriter.Options options, @NotNull Map<FilePath, Change> changes, boolean backup) throws IOException {
        try (ProgressMonitor.Task task = monitor.begin("Build packfile", 1)) {
            try (PackfileWriter writer = new PackfileWriter()) {
                if (target != null) {
                    final Set<Long> hashes = changes.keySet().stream()
                        .map(FilePath::hash)
                        .collect(Collectors.toSet());

                    for (PackfileBase.FileEntry file : target.getFileEntries()) {
                        if (!hashes.contains(file.hash())) {
                            writer.add(new PackfileResource(target, file));
                        }
                    }
                }

                for (Change change : changes.values()) {
                    writer.add(change.toResource());
                }

                final Path result = Path.of(path + ".tmp");

                try (FileChannel channel = FileChannel.open(result, WRITE, CREATE, TRUNCATE_EXISTING)) {
                    writer.write(monitor, channel, root.getProject().getCompressor(), options);
                }

                if (backup && Files.exists(path)) {
                    try {
                        Files.move(path, makeBackupPath(path));
                    } catch (IOException e) {
                        UIUtils.showErrorDialog(Application.getFrame(), e, "Unable to create backup");
                    }
                }

                Files.deleteIfExists(path);
                Files.move(result, path);
            }

            task.worked(1);
        }
    }

    @NotNull
    private Path makeBackupPath(@NotNull Path path) {
        for (int suffix = 0; ; suffix++) {
            final Path result;

            if (suffix == 0) {
                result = Path.of(path + ".bak");
            } else {
                result = Path.of(path + ".bak" + suffix);
            }

            if (Files.notExists(result)) {
                return result;
            }
        }
    }

    private record CompressionLevel(@NotNull Compressor.Level level, @NotNull String name, @Nullable String description) {}

    private record PackfileType(@NotNull String name, EnumSet<GameType> games) {}
}
