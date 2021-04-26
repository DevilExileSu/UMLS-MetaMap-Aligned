import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainExtract {
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1L;
    private  static final int NUM_POOL = 10;

    public static String SENTENCE_FILE = "/home/su/UMLS/left_data.4.26.txt";
    public static String RESULT_FILE = "/home/su/UMLS/test.txt";
    private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder(); // or "ISO-8859-1" for ISO Latin 1
    private static boolean isPureAscii(String v) {
        return asciiEncoder.canEncode(v);
    }
    public static void main(String[] args) throws  Exception {
        BufferedReader sentenceFileBufferedReader = new BufferedReader(new FileReader(SENTENCE_FILE));
        BufferedWriter resultBufferedWriter = new BufferedWriter(new FileWriter(RESULT_FILE, true));
        String line = "";
        ArrayList<String> sentences = new ArrayList<>();
        while ((line = sentenceFileBufferedReader.readLine()) != null) {
            if(!isPureAscii(line)) continue;
            sentences.add(line);
        }
        Tqdm tqdm = Tqdm.tqdm(sentences.size(), "Extract");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < NUM_POOL; i++) {
            Runnable worker = new Extract(sentences, resultBufferedWriter, tqdm);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        resultBufferedWriter.close();
        System.out.println("Finished all threads");
    }
}
