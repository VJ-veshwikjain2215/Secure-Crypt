package com.securecrypt;
import com.securecrypt.config.ApiSafetyFilter;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import static org.junit.jupiter.api.Assertions.*;
class ApiSafetyFilterTest {
 @Test void sharedBudgetIsBoundedAndStaticFilesRemainAvailable() throws Exception {
  var filter=new ApiSafetyFilter();
  for(int i=0;i<120;i++){var request=new MockHttpServletRequest("GET","/api/test");var response=new MockHttpServletResponse();filter.doFilter(request,response,new MockFilterChain());assertEquals(200,response.getStatus());}
  var response=new MockHttpServletResponse();filter.doFilter(new MockHttpServletRequest("GET","/api/test"),response,new MockFilterChain());assertEquals(429,response.getStatus());assertEquals("60",response.getHeader("Retry-After"));
  response=new MockHttpServletResponse();filter.doFilter(new MockHttpServletRequest("GET","/"),response,new MockFilterChain());assertEquals(200,response.getStatus());
 }
}
