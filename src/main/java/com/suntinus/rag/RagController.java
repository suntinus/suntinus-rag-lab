package com.suntinus.rag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.suntinus.rag.Records.*;
@RestController
@RequestMapping("/api")
public class RagController {
    private final RagService rag;
    public RagController(RagService rag){this.rag=rag;}
    @GetMapping("/backends") public List<String> backends(){return List.of("spring-ai","langchain4j");}
    @PostMapping("/{backend}/documents") public IngestResult ingest(@PathVariable String backend,@Valid @RequestBody DocumentInput input){return rag.ingest(backend,input);}
    @PostMapping("/{backend}/query") public Answer query(@PathVariable String backend,@Valid @RequestBody QueryInput input){return rag.ask(backend,input);}
    @PostMapping("/{backend}/search") public List<Hit> search(@PathVariable String backend,@Valid @RequestBody QueryInput input){return rag.retrieve(backend,input);}
}
