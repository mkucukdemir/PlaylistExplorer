package com.inforetros.command;

import com.inforetros.app.PlaylistExplorer;
import com.inforetros.option.BuildIndexer;
import com.inforetros.option.Searcher;
import picocli.CommandLine;

import java.io.File;
import java.util.concurrent.Callable;
@CommandLine.Command(name = "search", mixinStandardHelpOptions = true, version = "search 1.0", description = "Searches for the given query in Spotify playlists")
public class SearchCommand implements Callable<Integer> {

    @CommandLine.Parameters(index = "0", description = "query string to search")
    String query;

    @CommandLine.Option(names = { "-n" }, description = "number of searching results", defaultValue = "10", paramLabel = "num-of-results")
    public static Integer NUMOFRESULTS = 10;

    @CommandLine.Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpRequested = false;

    @Override
    public Integer call() throws Exception {
        // 1. Check index
        File file = new File(PlaylistExplorer.INDEX_PATH);
        if (file.exists()) {
            System.out.println("The index file has been found");
        } else {
        // 1.1. If the corpus is not preprocessed, build index
            System.out.println("The index file could not be found. The data will be indexed please wait...");
            BuildIndexer indexer = new BuildIndexer();
            indexer.run();
            System.out.println("The data has been indexed.");
        }

        // 2. Give query string and perform the searching
        System.out.println(query + " is being searching...");
        Searcher searcher = new Searcher();
        searcher.run(query);

        // 3. Print the top
        return null;
    }
}
