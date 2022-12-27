package com.shade.decima.ui.dialogs;

import com.shade.decima.model.app.ProjectContainer;
import com.shade.decima.model.base.GameType;
import com.shade.decima.ui.controls.FileExtensionFilter;
import com.shade.decima.ui.controls.validators.ExistingFileValidator;
import com.shade.decima.ui.controls.validators.NotEmptyValidator;
import com.shade.platform.ui.dialogs.BaseEditDialog;
import com.shade.platform.ui.util.UIUtils;
import com.shade.util.NotNull;
import com.shade.util.Nullable;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.nio.file.Path;
import java.util.Objects;

public class ProjectEditDialog extends BaseEditDialog {
    private final boolean edit;

    private final JTextField projectUuid;
    private final JTextField projectName;
    private final JComboBox<GameType> projectType;
    private final JTextField executableFilePath;
    private final JTextField archiveFolderPath;
    private final JTextField compressorPath;
    private final JTextField rttiInfoFilePath;
    private final JTextField archiveInfoFilePath;
    private final JTextField fileListingsPath;

    public ProjectEditDialog(boolean edit) {
        super(edit ? "编辑项目" : "新项目");

        this.edit = edit;

        this.projectUuid = new JTextField();
        this.projectUuid.setEditable(false);
        this.projectName = new JTextField();
        this.projectType = new JComboBox<>(GameType.values());
        this.executableFilePath = new JTextField();
        this.archiveFolderPath = new JTextField();
        this.compressorPath = new JTextField();
        this.rttiInfoFilePath = new JTextField();
        this.archiveInfoFilePath = new JTextField();
        this.fileListingsPath = new JTextField();
    }

    @NotNull
    @Override
    protected JComponent createContentsPane() {
        final JPanel panel = new JPanel();
        panel.setLayout(new MigLayout("insets 0", "[fill][grow,fill,250lp]", ""));

        if (edit) {
            panel.add(new JLabel("项目 UUID:"));
            panel.add(projectUuid, "wrap");
            panel.add(new JSeparator(), "wrap,span");
        }

        {
            panel.add(new JLabel("项目名称:"));
            panel.add(projectName, "wrap");

            UIUtils.installInputValidator(projectName, new NotEmptyValidator(projectName), this);
        }

        {
            panel.add(new JLabel("项目类型:"));
            panel.add(projectType, "wrap");
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("可执行文件", "exe");

            final JLabel gameExecutablePathLabel = new JLabel("游戏可执行路径:");
            gameExecutablePathLabel.setToolTipText("游戏可执行文件的路径。可能是游戏目录中唯一的.exe文件.");

            panel.add(gameExecutablePathLabel);
            panel.add(executableFilePath, "wrap");

            UIUtils.addOpenFileAction(executableFilePath, "选择游戏可执行文件", filter);
            UIUtils.installInputValidator(executableFilePath, new ExistingFileValidator(executableFilePath, filter), this);
        }

        {
            final JLabel archiveFolderPathLabel = new JLabel("游戏包文件文件夹路径:");
            archiveFolderPathLabel.setToolTipText("包含游戏存档的文件夹的路径。在大多数情况下，它有一堆.bin文件.");

            panel.add(archiveFolderPathLabel);
            panel.add(archiveFolderPath, "wrap");

            UIUtils.addOpenDirectoryAction(archiveFolderPath, "选择包含游戏存档的文件夹");
            UIUtils.installInputValidator(archiveFolderPath, new ExistingFileValidator(archiveFolderPath, null), this);
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("Oodle 库文件", "dll");

            final JLabel compressorPathLabel = new JLabel("Oodle 库目录:");
            compressorPathLabel.setToolTipText("处理游戏存档数据所需的库。\n这是一个位于游戏文件夹中的.dll文件，其名称以oo2core开头.");

            panel.add(compressorPathLabel);
            panel.add(compressorPath, "wrap");

            UIUtils.addOpenFileAction(compressorPath, "选择Oodle库", filter);
            UIUtils.installInputValidator(compressorPath, new ExistingFileValidator(compressorPath, filter), this);
        }


        panel.add(new JSeparator(), "wrap,span");

        {
            final FileExtensionFilter filter = new FileExtensionFilter("RTTI信息", "json", "json.gz");

            panel.add(new JLabel("RTTI metadata 目录:"));
            panel.add(rttiInfoFilePath, "wrap");

            UIUtils.addOpenFileAction(rttiInfoFilePath, "选择 RTTI信息 文件", filter);
            UIUtils.installInputValidator(rttiInfoFilePath, new ExistingFileValidator(rttiInfoFilePath, filter), this);
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("archive信息", "json", "json.gz");

            final JLabel label = new JLabel("程序包文件 metadata path:");
            panel.add(label);
            panel.add(archiveInfoFilePath, "wrap");

            UIUtils.addOpenFileAction(archiveInfoFilePath, "选择archive信息文件", filter);
            UIUtils.installInputValidator(archiveInfoFilePath, new ExistingFileValidator(archiveInfoFilePath, filter, false), this);
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("文件列表", "txt", "txt.gz");

            final JLabel label = new JLabel("文件列表路径:");
            panel.add(label);
            panel.add(fileListingsPath, "wrap");

            UIUtils.addOpenFileAction(fileListingsPath, "选择包含文件列表的文件", filter);
            UIUtils.installInputValidator(fileListingsPath, new ExistingFileValidator(fileListingsPath, filter, false), this);
        }

        return panel;
    }

    @Nullable
    @Override
    protected JComponent getDefaultComponent() {
        return projectName;
    }

    public void load(@NotNull ProjectContainer container) {
        projectUuid.setText(container.getId().toString());
        projectName.setText(container.getName());
        projectType.setSelectedItem(container.getType());
        executableFilePath.setText(container.getExecutablePath().toString());
        archiveFolderPath.setText(container.getPackfilesPath().toString());
        compressorPath.setText(container.getCompressorPath().toString());
        rttiInfoFilePath.setText(container.getTypeMetadataPath().toString());
        archiveInfoFilePath.setText(container.getPackfileMetadataPath() == null ? null : container.getPackfileMetadataPath().toString());
        fileListingsPath.setText(container.getFileListingsPath() == null ? null : container.getFileListingsPath().toString());
    }

    public void save(@NotNull ProjectContainer container) {
        container.setName(projectName.getText());
        container.setType((GameType) Objects.requireNonNull(projectType.getSelectedItem()));
        container.setExecutablePath(Path.of(executableFilePath.getText()));
        container.setPackfilesPath(Path.of(archiveFolderPath.getText()));
        container.setCompressorPath(Path.of(compressorPath.getText()));
        container.setTypeMetadataPath(Path.of(rttiInfoFilePath.getText()));
        container.setPackfileMetadataPath(archiveInfoFilePath.getText().isEmpty() ? null : Path.of(archiveInfoFilePath.getText()));
        container.setFileListingsPath(fileListingsPath.getText().isEmpty() ? null : Path.of(fileListingsPath.getText()));
    }

    @Override
    public boolean isComplete() {
        return UIUtils.isValid(projectName)
            && UIUtils.isValid(executableFilePath)
            && UIUtils.isValid(archiveFolderPath)
            && UIUtils.isValid(rttiInfoFilePath)
            && UIUtils.isValid(compressorPath);
    }
}
