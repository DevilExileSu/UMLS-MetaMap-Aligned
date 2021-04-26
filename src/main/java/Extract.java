import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import gov.nih.nlm.nls.metamap.*;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Extract implements  Runnable{
    public static int next = 0;
    private List<String> sentences;
    private Writer resFile;
    private Tqdm tqdm;
    public Extract(List<String> sentences, Writer resFile, Tqdm tqdm) {
        this.sentences = sentences;
        this.resFile = resFile;
        this.tqdm = tqdm;
    }
    private boolean hasNext() {
        if (this.next >= this.sentences.size())  return false;
        return true;
    }
    private synchronized static int getNext() {
        next++;
        return next -1;
    }
    @Override
    public void run() {
        MetaMapApi api = new MetaMapApiImpl();
        String options = "--restrict_to_sts bdsy,blor,bpoc,gngm,tisu,dsyn,neop";
        api.setOptions(options);
        while(hasNext()) {
            synchronized (tqdm) {
                tqdm.update(1);
            }
            String sentence = this.sentences.get(getNext());
            List<Result> resultList = api.processCitationsFromString(sentence);
            if (resultList.isEmpty()) {
                continue;
            }
            Result result = resultList.get(0);
            try {
                for (Utterance utterance : result.getUtteranceList()) {
                    JSONObject data = new JSONObject();
                    data.put("sent", utterance.getString());
                    JSONArray entitys = new JSONArray();
                    for (PCM pcm : utterance.getPCMList()) {
                        ArrayList<Mapping> maps = new ArrayList<Mapping>(pcm.getMappingList());
                        if (maps.isEmpty()) {
                            continue;
                        }
                        for (Mapping map : maps) {
                            for (Ev mapEv : map.getEvList()) {
                                List<Position> positions = mapEv.getPositionalInfo();
                                for (Position position : positions) {
                                    JSONObject entity = new JSONObject();
                                    ArrayList<Integer> pos = new ArrayList<>();
                                    int start = position.getX();
                                    int end = start + position.getY();
                                    pos.add(start);
                                    pos.add(end);
                                    entity.put("pos", pos);
                                    entity.put("name", mapEv.getMatchedWords());
                                    entity.put("type", mapEv.getSemanticTypes());
                                    entity.put("cui", mapEv.getConceptId());
                                    entitys.add(entity);
                                }
                            }
                        }
                    }
                    if(!entitys.isEmpty())  {
                        data.put("entity", entitys);
                        writeResult(data);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private synchronized void writeResult(JSONObject res) throws IOException {
        resFile.write(res.toJSONString() + '\n');
    }
}
