package com.shade.decima.cli;

import com.shade.decima.cli.commands.DumpFilePaths;
import com.shade.decima.cli.commands.Projects;
import com.shade.decima.cli.converters.ProjectConverter;
import com.shade.decima.model.app.Project;
import com.shade.decima.model.app.Workspace;
import com.shade.util.NotNull;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(subcommands = {
    DumpFilePaths.class,
    Projects.class
})
public class ApplicationCLI {
    private static Workspace workspace;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "��ʾ�˰�����Ϣ")
    private boolean usageHelpRequested;

    public static void execute(@NotNull Workspace workspace, String[] args) {
        ApplicationCLI.workspace = workspace;

        final int status = new CommandLine(ApplicationCLI.class)
            .registerConverter(Project.class, new ProjectConverter())
            .execute(args);

        System.exit(status);
    }

    @NotNull
    public static Workspace getWorkspace() {
        return workspace;
    }
}
