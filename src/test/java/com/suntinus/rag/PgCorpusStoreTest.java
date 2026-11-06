package com.suntinus.rag;
import org.junit.jupiter.api.*;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.flywaydb.core.Flyway;
import static org.assertj.core.api.Assertions.*;
import java.util.List;
import com.suntinus.rag.Records.*;
@Testcontainers
class PgCorpusStoreTest {
    @Container static final PostgreSQLContainer<?> PG=new PostgreSQLContainer<>(DockerImageName.parse("pgvector/pgvector:0.8.1-pg17").asCompatibleSubstituteFor("postgres"));
    PgCorpusStore store;
    @BeforeEach void setup(){
        var ds=new DriverManagerDataSource(PG.getJdbcUrl(),PG.getUsername(),PG.getPassword());
        Flyway.configure().dataSource(ds).load().migrate();
        var jdbc=new JdbcTemplate(ds);jdbc.execute("TRUNCATE documents CASCADE");
        store=new PgCorpusStore(jdbc,new DataSourceTransactionManager(ds));
    }
    float[] vector(int position){float[] v=new float[1024];v[position]=1;return v;}
    DocumentInput doc(String dataset,String source,String topic){return new DocumentInput(dataset,source,"Teste",topic,"2026-11-06","synthetic","Documento fictício");}
    @Test void filtersDatasetTopicAndEmbeddingSpaceBeforeTopK() {
        store.replace(doc("a","one","x"),"hash","space","spring-ai",List.of(new Chunk(0,"A",vector(0))));
        store.replace(doc("b","two","x"),"hash","space","spring-ai",List.of(new Chunk(0,"B",vector(0))));
        store.replace(doc("a","three","y"),"hash","space","spring-ai",List.of(new Chunk(0,"C",vector(0))));
        assertThat(store.search("a","x","space",vector(0),5,.5)).extracting(Hit::source).containsExactly("one");
        assertThat(store.search("a",null,"different-model",vector(0),5,.5)).isEmpty();
    }
    @Test void replacesChunksAndRollsBackInvalidReplacement() {
        var d=doc("a","one","x");
        store.replace(d,"hash1","space","spring-ai",List.of(new Chunk(0,"original",vector(0)),new Chunk(1,"old tail",vector(0))));
        store.replace(d,"hash2","space","langchain4j",List.of(new Chunk(0,"new",vector(0))));
        assertThat(store.search("a",null,"space",vector(0),5,.5)).extracting(Hit::text).containsExactly("new");
        assertThatThrownBy(()->store.replace(d,"bad","space","spring-ai",List.of(new Chunk(0,"bad",vector(0)),new Chunk(0,"duplicate index",vector(0))))).isInstanceOf(Exception.class);
        assertThat(store.unchanged("a","one","hash2","space")).isTrue();
        assertThat(store.search("a",null,"space",vector(0),5,.5)).extracting(Hit::text).containsExactly("new");
    }
    @Test void rejectsWrongDimensionAndNonFiniteVectors(){
        assertThatThrownBy(()->PgCorpusStore.vector(new float[4])).isInstanceOf(IllegalArgumentException.class);
        var bad=vector(0);bad[4]=Float.NaN;assertThatThrownBy(()->PgCorpusStore.vector(bad)).isInstanceOf(IllegalArgumentException.class);
    }
}
