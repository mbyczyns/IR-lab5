import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class Searcher {

    public static void main(String[] args) {
        IndexReader reader = getIndexReader();
        assert reader != null;
        IndexSearcher indexSearcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();

        try {
            // 1) TERM QUERY: Searching for "mammal"
            System.out.println("1) Term Query: mammal");
            TermQuery tq1 = new TermQuery(new Term(Constants.content, analyzer.normalize(Constants.content, "mammal").utf8ToString()));
            printResultsForQuery(indexSearcher, tq1);

            // 2) TERM QUERY: Searching for "bird"
            System.out.println("2) Term Query: bird");
            TermQuery tq2 = new TermQuery(new Term(Constants.content, analyzer.normalize(Constants.content, "bird").utf8ToString()));
            printResultsForQuery(indexSearcher, tq2);

            // 3) BOOLEAN QUERY: Searching for "mammal OR bird"
            System.out.println("3) Boolean Query: mammal OR bird");
            BooleanQuery booleanQuery = new BooleanQuery.Builder()
                    .add(new BooleanClause(tq1, BooleanClause.Occur.SHOULD))
                    .add(new BooleanClause(tq2, BooleanClause.Occur.SHOULD))
                    .setMinimumNumberShouldMatch(1)
                    .build();
            printResultsForQuery(indexSearcher, booleanQuery);

            // 4) RANGE QUERY: Searching for files smaller than 1000 bytes
            System.out.println("4) Range Query: File size [0b, 1000b]");
            Query rangeQuery = IntPoint.newRangeQuery(Constants.filesize, 0, 1000);
            printResultsForQuery(indexSearcher, rangeQuery);

            // 5) PREFIX QUERY: Searching for files starting with "ant"
            System.out.println("5) Prefix Query: ant");
            Query prefixQuery = new PrefixQuery(new Term(Constants.filename, "ant"));
            printResultsForQuery(indexSearcher, prefixQuery);

            // 6) WILDCARD QUERY: Searching for words matching "eat?"
            System.out.println("6) Wildcard Query: eat?");
            Query wildcardQuery = new WildcardQuery(new Term(Constants.content, "eat?"));
            printResultsForQuery(indexSearcher, wildcardQuery);

            // 7) FUZZY QUERY: Searching for "mamml"
            System.out.println("7) Fuzzy Query: mamml");
            Query fuzzyQuery = new FuzzyQuery(new Term(Constants.content, "mamml"));
            printResultsForQuery(indexSearcher, fuzzyQuery);

            // 8) QUERY PARSER: Parsing complex human queries
            System.out.println("8) Query Parser: " + "MaMMal AND bat");
            QueryParser parser = new QueryParser(Constants.content, analyzer);
            Query parsedQuery = parser.parse("MaMMal AND bat");
            printResultsForQuery(indexSearcher, parsedQuery);

        } catch (IOException | ParseException e) {
//            e.printStackTrace();
        }

        try {
            reader.close();
        } catch (IOException e) {
//            e.printStackTrace();
        }
    }

    private static void printResultsForQuery(IndexSearcher indexSearcher, Query q) throws IOException {
        TopDocs topDocs = indexSearcher.search(q, Constants.top_docs);
        System.out.println("Found " + topDocs.totalHits + " documents.");

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = indexSearcher.doc(scoreDoc.doc);
            System.out.println("SCORE: " + scoreDoc.score + " | FILENAME: " + doc.get(Constants.filename) +
                    " (ID=" + doc.get(Constants.id) + ") (Content=" + doc.get(Constants.content) + ") (Size=" + doc.get(Constants.filesize_int) + ")");
        }
    }

    private static IndexReader getIndexReader() {
        try {
            Directory dir = FSDirectory.open(Paths.get(Constants.index_dir));
            return DirectoryReader.open(dir);
        } catch (IOException e) {
//            e.printStackTrace();
        }
        return null;
    }
}
