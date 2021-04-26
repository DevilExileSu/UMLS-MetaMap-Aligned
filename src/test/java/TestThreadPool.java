import com.alibaba.fastjson.JSONObject;
import org.omg.PortableInterceptor.INACTIVE;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestThreadPool implements Runnable{
    private static int next = 0;
    private List<Map> data;
    private List<String[]> triples;
    private Tqdm tqdm;
    private Writer resFile;

    public TestThreadPool(List<Map> data,Tqdm tqdm,  List<String[]> triples, Writer resFile) {
        this.data = data;
        this.tqdm = tqdm;
        this.resFile = resFile;
        this.triples = triples;
    }
    private boolean hasNext() {
        if (next >= this.data.size())  return false;
        return true;
    }
    private synchronized static int getNext() {
        next++;
//
        return next -1;
    }

    @Override
    public void run() {
        while(hasNext()) {
            synchronized (tqdm) {
                tqdm.update(1);
            }
            Map item = data.get(getNext());
            JSONObject res = new JSONObject();
            res.put("sent", item.get("sent"));
            List<Map> entitys = (List<Map>) item.get("entity");
            if (entitys.size() < 2) {
                continue;
            }
            for(int i=0;i<entitys.size();i++) {
                String subj = (String) entitys.get(i).get("cui");
                ArrayList<String[]> hasSubjTriple = new ArrayList<>();
                for(String[] triple : triples) {
                    if(triple[0].equals(subj)) {
                        hasSubjTriple.add(triple);
                    }
                }
                for(int j=i+1; j<entitys.size();j++) {
                    String obj = (String) entitys.get(j).get("cui");
                    JSONObject resSubj = new JSONObject();
                    JSONObject resObj = new JSONObject();
                    resSubj.put("cui", subj);
                    resObj.put("cui", obj);
                    resSubj.put("name", entitys.get(i).get("name"));
                    resObj.put("name", entitys.get(j).get("name"));
                    resSubj.put("pos", entitys.get(i).get("pos"));
                    resObj.put("pos", entitys.get(j).get("pos"));
                    res.put("subj", resSubj);
                    res.put("obj", resObj);
                    try{
                    writeResult(res);
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    private synchronized void writeResult(JSONObject res) throws IOException {

        resFile.write(res.toJSONString() + '\n');
    }

}
