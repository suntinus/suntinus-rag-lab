package com.suntinus.rag;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import static org.assertj.core.api.Assertions.*;
class ApiTokenFilterTest {
    @Test void rejectsAnonymousCorpusAccessAndAllowsAuthorizedRequest() throws Exception {
        var filter=new ApiTokenFilter("a".repeat(32));
        var request=new MockHttpServletRequest("POST","/api/spring-ai/query");var response=new MockHttpServletResponse();
        filter.doFilter(request,response,new MockFilterChain());assertThat(response.getStatus()).isEqualTo(401);
        request=new MockHttpServletRequest("POST","/api/spring-ai/query");response=new MockHttpServletResponse();
        request.addHeader("Authorization","Bearer "+"a".repeat(32));var chain=new MockFilterChain();
        filter.doFilter(request,response,chain);assertThat(chain.getRequest()).isNotNull();
    }
}
