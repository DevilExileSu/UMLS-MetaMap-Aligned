import com.alibaba.fastjson.JSON;

import java.io.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Test {
    private static final int CORE_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 10;
    private static final int QUEUE_CAPACITY = 100;
    private static final Long KEEP_ALIVE_TIME = 1000L;
    private  static final int NUM_POOL = 10;

    public static String TRIPLE_FILEPATH = "/home/su/UMLS/MRREL.RRF";
    //    public static  String DATA_FILE = "/home/su/UMLS/test3.txt";
    public static  String DATA_FILE = "/home/su/UMLS/aligned_test.txt";
    public static String RESULT_FILEPATH = "/home/su/UMLS/res1.txt";


    public static  void main(String[] args) throws IOException {
        BufferedReader tripleBufferedReader = new BufferedReader(new FileReader(TRIPLE_FILEPATH));
        BufferedReader dataBufferedReader = new BufferedReader(new FileReader(DATA_FILE));
        BufferedWriter resultBufferedWriter= new BufferedWriter(new FileWriter(RESULT_FILEPATH, true));

        String line = "";
        ArrayList<String[]> triples = new ArrayList<>();
        while((line = tripleBufferedReader.readLine()) != null) {
            String[] item = line.split("\\|");
            triples.add(new String[]{item[4], item[7], item[0]});
        }
        ArrayList<Map> dataList = new ArrayList<>();
        while((line = dataBufferedReader.readLine()) != null) {
            Map<String, Object> item = JSON.parseObject(line, Map.class);
            dataList.add(item);
        }
        Tqdm tqdm = Tqdm.tqdm(dataList.size(), "Aligned");
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                CORE_POOL_SIZE,
                MAX_POOL_SIZE,
                KEEP_ALIVE_TIME,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(QUEUE_CAPACITY),
                new ThreadPoolExecutor.CallerRunsPolicy());
        for (int i = 0; i < NUM_POOL; i++) {
            //创建WorkerThread对象（WorkerThread类实现了Runnable 接口）
//            List<Map> subDataList = dataList.subList(i*subDataSize,  i==NUM_POOL-1?dataList.size() : i*subDataSize + subDataSize );
//            Runnable worker = new Aligned(triples, subDataList,resultBufferedWriter, tqdm);
            Runnable worker = new TestThreadPool(dataList, tqdm, triples, resultBufferedWriter);
            executor.execute(worker);
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
        }
        resultBufferedWriter.close();
        System.out.println("Finished all threads");
    }
}
