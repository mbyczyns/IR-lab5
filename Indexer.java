import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;

public class Indexer
{
    public static void main(String[] args) throws IOException
    {
        Indexer indexer = new Indexer();
        indexer.indexDocuments();
    }

    private void indexDocuments()
    {
        // REMOVE PREVIOUSLY GENERATED INDEX (DONE)
        try
        {
            FileUtils.deleteDirectory(new File(Constants.index_dir));
        } catch (IOException ignored)
        {
        }

        // LOAD HTML DOCUMENTS (TODO)
        ArrayList<Document> documents = getHTMLDocuments();


        // CONSTRUCT INDEX (TODO)
        // - Firstly, create Analyzer object (StandardAnalyzer).
        //   (An Analyzer builds TokenStreams, which analyze text.
        //   It thus represents a policy for extracting index terms from text.)
        // - Then, create IndexWriterConfig object that uses standard analyzer
        // - Construct IndexWriter (you can use FSDirectory.open and Paths.get + Constants.index_dir
        // - Add documents to the index
        // - Commit and close the index.

        // ----------------------------------

        // ----------------------------------

        if (documents == null || documents.isEmpty()) {
            System.out.println("Brak dokumentów do zindeksowania.");
            return;
        }

        // CONSTRUCT INDEX
        try {
            // 1. Tworzymy analyzer (StandardAnalyzer)
            StandardAnalyzer analyzer = new StandardAnalyzer();

            // 2. Tworzymy konfigurację dla IndexWriter (używając analyzer'a)
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            // 3. Tworzymy IndexWriter (FSDirectory przechowuje indeks na dysku)
            IndexWriter writer = new IndexWriter(FSDirectory.open(Paths.get(Constants.index_dir)), config);

            // 4. Dodajemy dokumenty do indeksu
            for (Document doc : documents) {
                writer.addDocument(doc);
                System.out.println("Dodano dokument do indeksu: " + doc.get(Constants.filename));
            }

            // 5. Zapisujemy i zamykamy indeks
            writer.commit();
            writer.close();

            System.out.println("Indeksowanie zakończone! Pliki zapisane w: " + Constants.index_dir);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private ArrayList<Document> getHTMLDocuments()
    {
        // This method is finished. Find getHTMLDocument
        File dir = new File("pages");
        File[] files = dir.listFiles();
        if (files != null)
        {
            ArrayList<Document> htmls = new ArrayList<>(files.length);
            for (int id = 0; id < files.length; id++)
            {
                System.out.println("Loading "+ files[id].getName());
                // TODO finish getHTML document
                htmls.add(getHTMLDocument("pages/" + files[id].getName(), id));
            }
            return htmls;
        }
        return null;
    }

    private Document getHTMLDocument(String path, int id)
    {
        File file = new File(path);
        Document document = new Document();

        /*Expert: directly create a field for a document.
        Most users should use one of the sugar subclasses:

        TextField: Reader or String indexed for full-text search
        StringField: String indexed verbatim as a single token
        IntPoint: int indexed for exact/range queries.
        LongPoint: long indexed for exact/range queries.
        FloatPoint: float indexed for exact/range queries.
        DoublePoint: double indexed for exact/range queries.
        SortedDocValuesField: byte[] indexed column-wise for sorting/faceting
        SortedSetDocValuesField: SortedSet<byte[]> indexed column-wise for sorting/faceting
        NumericDocValuesField: long indexed column-wise for sorting/faceting
        SortedNumericDocValuesField: SortedSet<long> indexed column-wise for sorting/faceting
        StoredField: Stored-only value for retrieving in summary results

        A field is a section of a Document.
        Each field has three parts: name, type and value.
        Values may be text (String, Reader or pre-analyzed TokenStream),
        binary (byte[]), or numeric (a Number). Fields are optionally
        stored in the index, so that they may be returned with hits on the document.
        */

        // STORED but NOT INDEXED: Document ID
        document.add(new StoredField(Constants.id, id));
        System.out.println("    - ID: " + id);

        // INDEXED but NOT STORED: Document Content (extracted with Tika)
        String content = getTextFromHTMLFile(file);
        if (content != null) {
            document.add(new TextField(Constants.content, content, Field.Store.NO));
            System.out.println("    - Content length: " + content.length() + " characters");
            System.out.println("     - content: " + content);

        }

        // STORED and INDEXED: File Name
        document.add(new TextField(Constants.filename, file.getName(), Field.Store.YES));
        System.out.println("    - Filename: " + file.getName());


        // INT FIELD (IntPoint) for file size (INDEXED but NOT STORED)
        long fileSize = file.length();
        document.add(new IntPoint(Constants.filesize, (int) fileSize));
        System.out.println("    - File size: " + fileSize + " bytes");


        // STORED FIELD for file size (to be retrievable)
        document.add(new StoredField(Constants.filesize, fileSize));


        return document;

    }

    // (DONE)
    private String getTextFromHTMLFile(File file)
    {
        BodyContentHandler handler = new BodyContentHandler();
        Metadata metadata = new Metadata();
        FileInputStream inputStream;
        try
        {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return null;
        }

        ParseContext pContext = new ParseContext();

        //Html parser
        HtmlParser htmlparser = new HtmlParser();
        try
        {
            htmlparser.parse(inputStream, handler, metadata, pContext);
        } catch (IOException | SAXException | TikaException e)
        {
            e.printStackTrace();
        }

        return handler.toString();
    }

}
