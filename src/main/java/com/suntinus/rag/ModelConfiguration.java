package com.suntinus.rag;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ai.ollama.*;
import org.springframework.ai.ollama.api.*;
import java.time.Duration;
import java.util.List;
@Configuration
public class ModelConfiguration {
    @Bean ModelPort springAi(@Value("${lab.ollama-url}") String url,@Value("${lab.embedding-model}") String embedding,@Value("${lab.chat-model}") String chat) {
        var requests=new org.springframework.http.client.SimpleClientHttpRequestFactory();
        requests.setConnectTimeout(Duration.ofSeconds(5));requests.setReadTimeout(Duration.ofMinutes(3));
        var api=OllamaApi.builder().baseUrl(url).restClientBuilder(org.springframework.web.client.RestClient.builder().requestFactory(requests)).build();
        var embeddings=OllamaEmbeddingModel.builder().ollamaApi(api).defaultOptions(OllamaEmbeddingOptions.builder().model(embedding).build()).build();
        var generator=OllamaChatModel.builder().ollamaApi(api).defaultOptions(OllamaChatOptions.builder().model(chat).temperature(0.0).numPredict(512).numCtx(8192).build()).build();
        return new ModelPort() {
            public String name(){return "spring-ai";}
            public float[] embed(String text){return embeddings.embed(text);}
            public List<float[]> embedAll(List<String> texts){return embeddings.embed(texts);}
            public String answer(String system,String user){return generator.call(new org.springframework.ai.chat.prompt.Prompt(List.of(new org.springframework.ai.chat.messages.SystemMessage(system),new org.springframework.ai.chat.messages.UserMessage(user)))).getResult().getOutput().getText();}
        };
    }
    @Bean ModelPort langchain4j(@Value("${lab.ollama-url}") String url,@Value("${lab.embedding-model}") String embedding,@Value("${lab.chat-model}") String chat) {
        var embeddings=dev.langchain4j.model.ollama.OllamaEmbeddingModel.builder().baseUrl(url).modelName(embedding).timeout(Duration.ofMinutes(3)).build();
        var generator=dev.langchain4j.model.ollama.OllamaChatModel.builder().baseUrl(url).modelName(chat).temperature(0.0).numPredict(512).numCtx(8192).timeout(Duration.ofMinutes(3)).build();
        return new ModelPort() {
            public String name(){return "langchain4j";}
            public float[] embed(String text){return embeddings.embed(text).content().vector();}
            public List<float[]> embedAll(List<String> texts){return embeddings.embedAll(texts.stream().map(dev.langchain4j.data.segment.TextSegment::from).toList()).content().stream().map(dev.langchain4j.data.embedding.Embedding::vector).toList();}
            public String answer(String system,String user){return generator.chat(List.of(dev.langchain4j.data.message.SystemMessage.from(system),dev.langchain4j.data.message.UserMessage.from(user))).aiMessage().text();}
        };
    }
    @Bean RagService ragService(CorpusStore store,List<ModelPort> models,@Value("${lab.embedding-model}") String model,@Value("${lab.embedding-revision}") String revision,@Value("${lab.ollama-url}") String url,com.fasterxml.jackson.databind.ObjectMapper mapper) throws Exception {
        if(revision.isBlank() || revision.equals("not-pulled"))throw new IllegalStateException("Run scripts/pull-models.ps1 before starting the app");
        var client=java.net.http.HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        var response=client.send(java.net.http.HttpRequest.newBuilder(java.net.URI.create(url+"/api/tags")).timeout(Duration.ofSeconds(10)).GET().build(),java.net.http.HttpResponse.BodyHandlers.ofString());
        boolean matches=false;
        if(response.statusCode()==200)for(var entry:mapper.readTree(response.body()).path("models")) {
            if(entry.path("name").asText().equals(model) && entry.path("digest").asText().equals(revision))matches=true;
        }
        if(!matches)throw new IllegalStateException("Embedding model digest changed or unavailable. Pull models, restart and reindex the corpus.");
        return new RagService(store,models,model+"|"+revision+"|1024|unicode-1200-160-v1");
    }
}
