package com.mapvendor.module.business;
import static org.assertj.core.api.Assertions.assertThat;
import java.io.InputStream; import java.nio.file.Files; import java.nio.file.Paths; import java.util.Map;
import org.junit.jupiter.api.Test; import org.yaml.snakeyaml.Yaml;
class AdminBusinessContractTest {
 @Test void contractContainsBusinessOperationsAndNoSecurity() throws Exception {
  Map<?,?> doc; try(InputStream in=Files.newInputStream(Paths.get("docs/openapi/admin-api-contract-v1.yaml"))){doc=new Yaml().load(in);}
  Map<?,?> paths=(Map<?,?>)doc.get("paths");
  assertThat(paths.keySet()).contains("/api/v1/admin/businesses","/api/v1/admin/businesses/{id}","/api/v1/admin/businesses/{id}/status",
   "/api/v1/admin/businesses/{businessId}/cars","/api/v1/admin/businesses/{businessId}/cars/{id}",
   "/api/v1/admin/businesses/{businessId}/rooms","/api/v1/admin/businesses/{businessId}/rooms/{id}",
   "/api/v1/admin/businesses/{businessId}/dishes","/api/v1/admin/businesses/{businessId}/dishes/{id}");
  assertThat(doc.containsKey("security")).isFalse();
 }
}
