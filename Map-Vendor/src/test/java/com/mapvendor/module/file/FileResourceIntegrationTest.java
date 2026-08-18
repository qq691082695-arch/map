package com.mapvendor.module.file;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.MapVendorApplication;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes=MapVendorApplication.class,properties={
 "spring.datasource.url=jdbc:h2:mem:fileresource;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
 "spring.datasource.username=sa","spring.datasource.password=","spring.flyway.enabled=false",
 "map-vendor.storage.root=target/test-file-storage","map-vendor.storage.public-base-url=/files",
 "map-vendor.storage.max-file-size-bytes=1024"})
@AutoConfigureMockMvc
@Sql("classpath:file-resource-schema.sql")
class FileResourceIntegrationTest {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc; @Autowired ObjectMapper json;

 @Test void uploadsPersistsAndServesImage() throws Exception {
  MockMultipartFile file=new MockMultipartFile("file","map.png","image/png",png());
  String body=mvc.perform(multipart("/api/v1/admin/files/images").file(file).header("X-Request-Id","file-upload"))
   .andExpect(status().isOk()).andExpect(jsonPath("$.data.resourceId").isNumber())
   .andExpect(jsonPath("$.data.url").value(org.hamcrest.Matchers.startsWith("/files/")))
   .andExpect(jsonPath("$.data.mimeType").value("image/png")).andReturn().getResponse().getContentAsString();
  JsonNode data=json.readTree(body).path("data"); String url=data.path("url").asText();
  String key=url.substring("/files/".length());
  assertThat(jdbc.queryForObject("SELECT storage_key FROM file_resource WHERE id=?",String.class,data.path("resourceId").asLong())).isEqualTo(key);
  assertThat(Files.isRegularFile(Paths.get("target/test-file-storage").resolve(key))).isTrue();
  mvc.perform(get(url)).andExpect(status().isOk()).andExpect(header().string("Content-Type","image/png"))
   .andExpect(header().string("X-Content-Type-Options","nosniff")).andExpect(content().bytes(png()));
 }

 @Test void rejectsDisguisedAndTraversalUploads() throws Exception {
  mvc.perform(multipart("/api/v1/admin/files/images").file(new MockMultipartFile("file","bad.png","image/png","<script>x</script>".getBytes("UTF-8"))))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
  mvc.perform(multipart("/api/v1/admin/files/images").file(new MockMultipartFile("file","../bad.png","image/png",png())))
   .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("INVALID_IMAGE"));
  assertThat(jdbc.queryForObject("SELECT COUNT(*) FROM file_resource",Integer.class)).isZero();
 }

 @Test void publishesMultipartOpenApiContract() throws Exception {
  mvc.perform(get("/v3/api-docs/admin")).andExpect(status().isOk())
   .andExpect(jsonPath("$.paths['/api/v1/admin/files/images'].post.requestBody.content['multipart/form-data']").exists());
 }

 private byte[] png(){return Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNk+A8AAQUBAScY42YAAAAASUVORK5CYII=");}
}
