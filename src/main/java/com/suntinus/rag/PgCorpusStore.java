package com.suntinus.rag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.support.TransactionTemplate;
import java.util.List;
import com.suntinus.rag.Records.*;
@Repository
public class PgCorpusStore implements CorpusStore {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate transactions;
    public PgCorpusStore(JdbcTemplate jdbc,org.springframework.transaction.PlatformTransactionManager manager){this.jdbc=jdbc;this.transactions=new TransactionTemplate(manager);}
    public boolean unchanged(String dataset,String source,String hash,String space) {
        return Boolean.TRUE.equals(jdbc.queryForObject("SELECT EXISTS(SELECT 1 FROM documents WHERE dataset=? AND source=? AND content_hash=? AND embedding_space=?)",Boolean.class,dataset,source,hash,space));
    }
    public void replace(DocumentInput input,String hash,String space,String backend,List<Chunk> chunks) {
        chunks.forEach(c->vector(c.embedding()));
        transactions.executeWithoutResult(status->{
            jdbc.query("SELECT pg_advisory_xact_lock(hashtextextended(?,0))",rs->{},input.dataset()+":"+input.source());
            jdbc.update("DELETE FROM documents WHERE dataset=? AND source=?",input.dataset(),input.source());
            jdbc.update("INSERT INTO documents(dataset,source,title,topic,source_date,kind,content_hash,embedding_space,backend) VALUES(?,?,?,?,?::date,?,?,?,?)",input.dataset(),input.source(),input.title(),input.topic(),input.date(),input.kind(),hash,space,backend);
            jdbc.batchUpdate("INSERT INTO chunks(dataset,source,chunk_index,content,embedding) VALUES(?,?,?,?,?::vector)",chunks,chunks.size(),(ps,c)->{
                ps.setString(1,input.dataset());ps.setString(2,input.source());ps.setInt(3,c.index());ps.setString(4,c.text());ps.setString(5,vector(c.embedding()));
            });
        });
    }
    public List<Hit> search(String dataset,String topic,String space,float[] embedding,int topK,double minScore) {
        String vector=vector(embedding);
        return jdbc.query("""
            SELECT d.source,d.title,d.topic,d.source_date,d.kind,c.chunk_index,c.content,
                   1-(c.embedding <=> ?::vector) AS score
            FROM chunks c JOIN documents d USING(dataset,source)
            WHERE d.dataset=? AND d.embedding_space=? AND (?::text IS NULL OR d.topic=?)
              AND 1-(c.embedding <=> ?::vector)>=?
            ORDER BY c.embedding <=> ?::vector, d.source,c.chunk_index LIMIT ?
            """,(rs,row)->new Hit(rs.getString("source"),rs.getString("title"),rs.getString("topic"),rs.getString("source_date"),rs.getString("kind"),rs.getInt("chunk_index"),rs.getString("content"),rs.getDouble("score")),vector,dataset,space,topic,topic,vector,minScore,vector,topK);
    }
    static String vector(float[] values) {
        if(values==null || values.length!=1024)throw new IllegalArgumentException("Embedding must have 1024 dimensions; reindex after model changes");
        double norm=0;
        for(float value:values){if(!Float.isFinite(value))throw new IllegalArgumentException("Non-finite embedding");norm+=value*value;}
        if(norm==0)throw new IllegalArgumentException("Zero embedding");
        return java.util.Arrays.toString(values);
    }
}
