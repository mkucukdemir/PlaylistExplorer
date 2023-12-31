package com.inforetros.option;

import com.inforetros.app.PlaylistExplorer;
import com.inforetros.util.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;

import static com.inforetros.command.SearchCommand.NUMOFRESULTS;

public class Searcher {

    public void run(String qstr) {
        try {
            // Analyzer specifies options for text tokenization and normalization (e.g., stemming, stop words removal, case-folding)
            Analyzer analyzer = new Analyzer() {
                @Override
                protected TokenStreamComponents createComponents( String fieldName ) {
                    // Step 1: tokenization (Lucene's StandardTokenizer is suitable for most text retrieval occasions)
                    // StandardTokenizer: Breaks the text into segments (words)
                    TokenStreamComponents ts = new TokenStreamComponents( new StandardTokenizer() );
                    // Step 2: transforming all tokens into lowercased ones (recommended for the majority of the problems)
                    ts = new TokenStreamComponents( ts.getSource(), new LowerCaseFilter( ts.getTokenStream() ) );
                    // Step 3: whether to remove stop words (unnecessary to remove stop words unless you can't afford the extra disk space)
                    // Uncomment the following line to remove stop words
                    ts = new TokenStreamComponents( ts.getSource(), new StopFilter( ts.getTokenStream(), EnglishAnalyzer.ENGLISH_STOP_WORDS_SET ) );
                    // Step 4: whether to apply stemming
                    // Uncomment one of the following two lines to apply Krovetz or Porter stemmer (Krovetz is more common for IR research)
                    // Krovetz stemmer: Hybrid algorithmic-dictionary-based method
                    ts = new TokenStreamComponents( ts.getSource(), new KStemFilter( ts.getTokenStream() ) );
                    // ts = new TokenStreamComponents( ts.getSource(), new PorterStemFilter( ts.getTokenStream() ) );
                    return ts;
                }
            };

            String field = "track_name"; // the field you hope to search for
            QueryParser parser = new QueryParser( field, analyzer ); // a query parser that transforms a text string into Lucene's query object

            Query query = parser.parse( qstr ); // this is Lucene's query object

            // Okay, now let's open an index and search for documents
            Directory dir = FSDirectory.open( new File(PlaylistExplorer.INDEX_PATH).toPath() );
            IndexReader index = DirectoryReader.open( dir );

            // you need to create a Lucene searcher
            IndexSearcher searcher = new IndexSearcher( index );

            // make sure the similarity class you are using is consistent with those being used for indexing
            searcher.setSimilarity( new BM25Similarity() );

            TopDocs docs = searcher.search( query, NUMOFRESULTS );

            System.out.printf( "%-10s%-20s%-10s%-20s%-20s%s\n", "Rank", "Playlist Name", "Score", "Artist Name", "Album", "Track Name" );
            int rank = 1;
            for ( ScoreDoc scoreDoc : docs.scoreDocs ) {
                int docid = scoreDoc.doc;
                double score = scoreDoc.score;
                String playlist_name = LuceneUtils.getDocno( index, "playlist_name", docid );
                String track_name = LuceneUtils.getDocno( index, "track_name", docid );
                String artist_name = LuceneUtils.getDocno( index, "artist_name", docid );
                String album_name = LuceneUtils.getDocno( index, "album_name", docid );
                System.out.printf( "%-10d%-20s%-10.4f%-20s%-20s%s\n", rank, playlist_name, score,artist_name, album_name, track_name );
                rank++;
            }

            // remember to close the index and the directory
            index.close();
            dir.close();

        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
