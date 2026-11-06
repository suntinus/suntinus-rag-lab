package com.suntinus.rag;
public interface ModelPort {
    String name();
    float[] embed(String text);
    default java.util.List<float[]> embedAll(java.util.List<String> texts) { return texts.stream().map(this::embed).toList(); }
    String answer(String system, String user);
}
