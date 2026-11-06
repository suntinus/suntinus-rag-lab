package com.suntinus.rag;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;
class ChunkerTest {
    @Test void preservesUnicodeAndOverlapWithoutDroppingTail() {
        var chunks=Chunker.split("ação😀teste memória",8,2);
        assertThat(chunks).isNotEmpty();
        StringBuilder rebuilt=new StringBuilder(chunks.getFirst());
        for(int i=1;i<chunks.size();i++) {
            var previous=chunks.get(i-1).codePoints().toArray();
            var next=chunks.get(i).codePoints().toArray();
            assertThat(java.util.Arrays.copyOf(next,2)).containsExactly(java.util.Arrays.copyOfRange(previous,previous.length-2,previous.length));
            rebuilt.append(new String(next,2,next.length-2));
        }
        assertThat(rebuilt.toString()).isEqualTo("ação😀teste memória");
    }
    @Test void rejectsNonProgressingOverlap() {
        assertThatThrownBy(()->Chunker.split("abc",3,3)).isInstanceOf(IllegalArgumentException.class);
    }
    @Test void blankInputHasNoChunks() { assertThat(Chunker.split(" \n",1200,160)).isEmpty(); }
}
