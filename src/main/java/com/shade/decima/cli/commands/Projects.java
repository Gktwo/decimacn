package com.shade.decima.cli.commands;

import picocli.CommandLine.Command;

@Command(name = "项目", description = "项目管理", subcommands = ProjectsList.class)
public class Projects {
}
