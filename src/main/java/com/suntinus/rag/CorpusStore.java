package com.suntinus.rag;
import java.util.List;
import com.suntinus.rag.Records.*;
public interface CorpusStore {
    boolean unchanged(String dataset,String source,String hash,String space);
    void replace(DocumentInput input,String hash,String space,String backend,List<Chunk> chunks);
    List<Hit> search(String dataset,String topic,String space,float[] vector,int topK,double minScore);
}
