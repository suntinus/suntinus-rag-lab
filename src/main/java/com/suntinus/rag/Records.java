package com.suntinus.rag;
import jakarta.validation.constraints.*;
import java.util.List;
public final class Records {
    private Records() {}
    public record DocumentInput(
        @NotBlank @Pattern(regexp="[a-zA-Z0-9_-]{1,64}") String dataset,
        @NotBlank @Size(max=200) String source,
        @NotBlank @Size(max=400) String title,
        @NotBlank @Size(max=100) String topic,
        @NotBlank @Pattern(regexp="\\d{4}-\\d{2}-\\d{2}") String date,
        @NotBlank @Pattern(regexp="historical-extract|curated-context|synthetic") String kind,
        @NotBlank @Size(max=100000) String text) {}
    public record QueryInput(
        @NotBlank @Pattern(regexp="[a-zA-Z0-9_-]{1,64}") String dataset,
        @NotBlank @Size(max=2000) String question,
        @Size(max=100) String topic,
        @Min(1) @Max(10) int topK,
        @DecimalMin("0.0") @DecimalMax("1.0") double minScore) {}
    public record Chunk(int index,String text,float[] embedding) {}
    public record Hit(String source,String title,String topic,String date,String kind,int chunk,String text,double score) {}
    public record IngestResult(String source,int chunks,boolean unchanged) {}
    public record Answer(String backend,String answer,List<Hit> sources,boolean abstained,long elapsedMs) {}
}
