import com.alibaba.fastjson.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class Aligned implements Runnable{
    private static int next = 0;
    private List<String[]> triples;
    private List<Map> data;
    private Writer resFile;
    private Tqdm tqdm;

    public Aligned( List<String[]> triples, List<Map> data, Writer resFile, Tqdm tqdm) {
        this.triples = triples;
        this.data = data;
        this.resFile = resFile;
        this.tqdm = tqdm;
    }
    private synchronized boolean hasNext() {
        if (next < this.data.size())  return true;
        return false;
    }
    private synchronized static int getNext() {
        next++;
        return next - 1;
    }
    @Override
     public void run(){
        while(hasNext()) {
            synchronized (tqdm) {
                tqdm.update(1);
            }
            Map item = this.data.get(getNext());
            List<Map> entitys = (List<Map>) item.get("entity");
            if (entitys.size() < 2) {
                continue;
            }
            for(int i=0; i < entitys.size(); i++) {
                String subj = (String) entitys.get(i).get("cui");
                 ArrayList<String[]> hasSubjTriple = new ArrayList<>();
                 for(String[] triple : triples) {
                     if(triple[0].equals(subj)) {
                         hasSubjTriple.add(triple);
                     }
                 }
                 for(int j=i+1; j<entitys.size(); j++) {
                     String obj = (String) entitys.get(j).get("cui");
                     JSONObject res = new JSONObject();
                     JSONObject resSubj = new JSONObject();
                     JSONObject resObj = new JSONObject();
                     res.put("sent", item.get("sent"));
                     resSubj.put("cui", subj);
                     resObj.put("cui", obj);
                     resSubj.put("name", entitys.get(i).get("name"));
                     resObj.put("name", entitys.get(j).get("name"));
                     resSubj.put("pos", entitys.get(i).get("pos"));
                     resObj.put("pos", entitys.get(j).get("pos"));
                     res.put("subj", resSubj);
                     res.put("obj", resObj);
                     if(hasSubjTriple.isEmpty()) {
                         res.put("relation", "NA");
                         try {
                             writeResult(res);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                         continue;
                     }
                     if(entitys.get(i).get("pos").equals(entitys.get(j).get("pos")) ||  entitys.get(i).get("name").equals(entitys.get(j).get("name"))) continue;;
                     ArrayList<String[]> hasObjTriple = new ArrayList<>();
                     for(String[] triple : hasSubjTriple) {
                         if(triple[2].equals(obj)) {
                             hasObjTriple.add(triple);
                         }
                     }
                     if(hasObjTriple.isEmpty()) {
                         res.put("relation", "NA");
                         try {
                             writeResult(res);
                         } catch (IOException e) {
                             e.printStackTrace();
                         }
                     } else {
                         for(String[] triple: hasObjTriple) {
                             res.put("relation", triple[1]);
                             try {
                                 writeResult(res);
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                         }
                     }
                 }
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private synchronized void writeResult(JSONObject res) throws IOException {
        resFile.write(res.toJSONString() + '\n');
    }
}
