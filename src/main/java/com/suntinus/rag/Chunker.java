package com.suntinus.rag;
import java.util.List;
public final class Chunker {
    public static List<String> split(String text, int size, int overlap) {
        if(size<1 || overlap<0 || overlap>=size) throw new IllegalArgumentException("Invalid chunk window");
        if(text==null || text.isBlank()) return List.of();
        int[] points=text.codePoints().toArray();
        var chunks=new java.util.ArrayList<String>();
        for(int start=0;start<points.length;start+=size-overlap) {
            int length=Math.min(size,points.length-start);
            chunks.add(new String(points,start,length));
            if(start+length==points.length) break;
        }
        return List.copyOf(chunks);
    }
}
