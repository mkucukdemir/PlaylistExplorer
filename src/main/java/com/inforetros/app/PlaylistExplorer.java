package com.inforetros.app;

import com.inforetros.command.SearchCommand;
import picocli.CommandLine;

public class PlaylistExplorer {
    public static final String INDEX_PATH = "kaggle.com\\index";
    public static final String JSON_FOLDER = "kaggle.com\\data\\subset";
    public static void main(String[] args) {
        int exitCode = new CommandLine(new SearchCommand()).execute(args);
        System.exit(exitCode);
    }
}
