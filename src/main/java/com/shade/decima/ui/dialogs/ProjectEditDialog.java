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
        super(edit ? "�༭��Ŀ" : "����Ŀ");

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
            panel.add(new JLabel("��Ŀ UUID:"));
            panel.add(projectUuid, "wrap");
            panel.add(new JSeparator(), "wrap,span");
        }

        {
            panel.add(new JLabel("��Ŀ����:"));
            panel.add(projectName, "wrap");

            UIUtils.installInputValidator(projectName, new NotEmptyValidator(projectName), this);
        }

        {
            panel.add(new JLabel("��Ŀ����:"));
            panel.add(projectType, "wrap");
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("��ִ���ļ�", "exe");

            final JLabel gameExecutablePathLabel = new JLabel("��Ϸ��ִ��·��:");
            gameExecutablePathLabel.setToolTipText("��Ϸ��ִ���ļ���·������������ϷĿ¼��Ψһ��.exe�ļ�.");

            panel.add(gameExecutablePathLabel);
            panel.add(executableFilePath, "wrap");

            UIUtils.addOpenFileAction(executableFilePath, "ѡ����Ϸ��ִ���ļ�", filter);
            UIUtils.installInputValidator(executableFilePath, new ExistingFileValidator(executableFilePath, filter), this);
        }

        {
            final JLabel archiveFolderPathLabel = new JLabel("��Ϸ���ļ��ļ���·��:");
            archiveFolderPathLabel.setToolTipText("������Ϸ�浵���ļ��е�·�����ڴ��������£�����һ��.bin�ļ�.");

            panel.add(archiveFolderPathLabel);
            panel.add(archiveFolderPath, "wrap");

            UIUtils.addOpenDirectoryAction(archiveFolderPath, "ѡ�������Ϸ�浵���ļ���");
            UIUtils.installInputValidator(archiveFolderPath, new ExistingFileValidator(archiveFolderPath, null), this);
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("Oodle ���ļ�", "dll");

            final JLabel compressorPathLabel = new JLabel("Oodle ��Ŀ¼:");
            compressorPathLabel.setToolTipText("������Ϸ�浵��������Ŀ⡣\n����һ��λ����Ϸ�ļ����е�.dll�ļ�����������oo2core��ͷ.");

            panel.add(compressorPathLabel);
            panel.add(compressorPath, "wrap");

            UIUtils.addOpenFileAction(compressorPath, "ѡ��Oodle��", filter);
            UIUtils.installInputValidator(compressorPath, new ExistingFileValidator(compressorPath, filter), this);
        }


        panel.add(new JSeparator(), "wrap,span");

        {
            final FileExtensionFilter filter = new FileExtensionFilter("RTTI��Ϣ", "json", "json.gz");

            panel.add(new JLabel("RTTI metadata Ŀ¼:"));
            panel.add(rttiInfoFilePath, "wrap");

            UIUtils.addOpenFileAction(rttiInfoFilePath, "ѡ�� RTTI��Ϣ �ļ�", filter);
            UIUtils.installInputValidator(rttiInfoFilePath, new ExistingFileValidator(rttiInfoFilePath, filter), this);
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("archive��Ϣ", "json", "json.gz");

            final JLabel label = new JLabel("������ļ� metadata path:");
            panel.add(label);
            panel.add(archiveInfoFilePath, "wrap");

            UIUtils.addOpenFileAction(archiveInfoFilePath, "ѡ��archive��Ϣ�ļ�", filter);
            UIUtils.installInputValidator(archiveInfoFilePath, new ExistingFileValidator(archiveInfoFilePath, filter, false), this);
        }

        {
            final FileExtensionFilter filter = new FileExtensionFilter("�ļ��б�", "txt", "txt.gz");

            final JLabel label = new JLabel("�ļ��б�·��:");
            panel.add(label);
            panel.add(fileListingsPath, "wrap");

            UIUtils.addOpenFileAction(fileListingsPath, "ѡ������ļ��б���ļ�", filter);
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
