package com.securecrypt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest @AutoConfigureMockMvc
class ApiContractTest {
 @Autowired MockMvc mvc;
 @Test void invalidRequestsAre400() throws Exception {
  mvc.perform(post("/api/encrypt").contentType(MediaType.APPLICATION_JSON).content("{bad")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Invalid request format or parameter type."));
  mvc.perform(get("/api/generate-key?length=abc")).andExpect(status().isBadRequest());mvc.perform(get("/api/generate-key?length=1000000")).andExpect(status().isBadRequest());
  mvc.perform(post("/api/encrypt").contentType(MediaType.APPLICATION_JSON).content("{\"text\":\"test\",\"algorithm\":\"<b>unsafe</b>\",\"key\":\"test\"}")).andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Unsupported algorithm."));
 }
 @Test void retiredStrengthAndHeaders() throws Exception {
  mvc.perform(get("/api/key-strength")).andExpect(status().isGone()).andExpect(header().string("Cache-Control","no-store"));mvc.perform(get("/api/generate-key?length=16")).andExpect(status().isOk()).andExpect(header().string("X-Content-Type-Options","nosniff")).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
 }
 @Test void largeBodyRejectedBeforeProcessing() throws Exception{mvc.perform(post("/api/encrypt").contentType(MediaType.APPLICATION_JSON).content("A".repeat(512001))).andExpect(status().isPayloadTooLarge());}
}
