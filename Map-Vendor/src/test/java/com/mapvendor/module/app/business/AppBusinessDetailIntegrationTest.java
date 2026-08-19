package com.mapvendor.module.app.business;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapvendor.MapVendorApplication;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(classes = MapVendorApplication.class, properties = {
        "spring.datasource.url=jdbc:h2:mem:appbusiness;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
@Sql(scripts = {"classpath:app-business-detail-schema.sql", "classpath:app-business-detail-data.sql"})
@SqlConfig(encoding = "UTF-8")
class AppBusinessDetailIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private JsonNode fetchJson(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        return objectMapper.readTree(body);
    }

    @Test
    void returnsTravelDetailWithOnlyVisibleOwnCarsAndActiveImages() throws Exception {
        JsonNode root = fetchJson("/api/v1/app/businesses/10");
        assertEquals("OK", root.get("code").asText());
        JsonNode common = root.at("/data/common");
        assertEquals(10L, common.get("id").asLong());
        assertEquals("出行商家", common.get("name").asText());
        assertEquals("TRAVEL", common.get("businessType").asText());
        assertEquals("地址甲", common.get("address").asText());
        assertEquals(121.1, common.get("longitude").asDouble());
        assertEquals(31.1, common.get("latitude").asDouble());
        assertEquals("出行简介", common.get("intro").asText());
        JsonNode imageUrls = common.get("imageUrls");
        assertEquals(1, imageUrls.size());
        assertEquals("/images/active.png", imageUrls.get(0).asText());

        JsonNode detail = root.at("/data/detail");
        assertEquals("TRAVEL", detail.get("kind").asText());
        JsonNode cars = detail.get("cars");
        assertEquals(1, cars.size());
        JsonNode car = cars.get(0);
        assertEquals(101L, car.get("id").asLong());
        assertEquals("别克GL8", car.get("model").asText());
        assertEquals(7, car.get("seatNum").asInt());
        assertEquals("商务接送", car.get("description").asText());
        assertEquals("/images/active.png", car.get("imageUrl").asText());
    }

    @Test
    void returnsHotelDetailWithOnlyVisibleOwnRooms() throws Exception {
        JsonNode root = fetchJson("/api/v1/app/businesses/11");
        assertEquals("住宿商家", root.at("/data/common/name").asText());
        assertEquals("HOTEL", root.at("/data/common/businessType").asText());
        assertEquals(0, root.at("/data/common/imageUrls").size());
        JsonNode detail = root.at("/data/detail");
        assertEquals("HOTEL", detail.get("kind").asText());
        JsonNode rooms = detail.get("rooms");
        assertEquals(1, rooms.size());
        JsonNode room = rooms.get(0);
        assertEquals(201L, room.get("id").asLong());
        assertEquals("商务套房", room.get("name").asText());
        assertEquals("1.8m大床", room.get("bedSpec").asText());
        assertEquals("含会客区", room.get("description").asText());
        assertEquals("/images/active.png", room.get("imageUrl").asText());
    }

    @Test
    void returnsFoodDetailWithDishesSortedBySortNoAndHiddenDisabledImage() throws Exception {
        JsonNode root = fetchJson("/api/v1/app/businesses/12");
        assertEquals("餐饮商家", root.at("/data/common/name").asText());
        assertEquals("FOOD", root.at("/data/common/businessType").asText());
        JsonNode detail = root.at("/data/detail");
        assertEquals("FOOD", detail.get("kind").asText());
        assertEquals("李经理", detail.get("contactName").asText());
        assertEquals("027-88888888", detail.get("contactPhone").asText());
        assertEquals("推荐菜一、推荐菜二", detail.get("recommendedDishes").asText());
        JsonNode dishes = detail.get("dishes");
        assertEquals(2, dishes.size());
        assertEquals(302L, dishes.get(0).get("id").asLong());
        assertEquals("推荐菜二", dishes.get(0).get("name").asText());
        assertTrue(dishes.get(0).get("imageUrl").isNull());
        assertEquals(301L, dishes.get(1).get("id").asLong());
        assertEquals("/images/active.png", dishes.get(1).get("imageUrl").asText());
    }

    @Test
    void disabledDeletedAndMissingBusinessAreNotFound() throws Exception {
        assertNotFound("/api/v1/app/businesses/13");
        assertNotFound("/api/v1/app/businesses/14");
        assertNotFound("/api/v1/app/businesses/999");
    }

    private void assertNotFound(String url) throws Exception {
        MvcResult result = mockMvc.perform(get(url)).andExpect(status().isNotFound()).andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertEquals("BUSINESS_NOT_FOUND", objectMapper.readTree(body).get("code").asText());
    }

    @Test
    void publishesDetailEndpointInGeneratedAppOpenApi() throws Exception {
        MvcResult result = mockMvc.perform(get("/v3/api-docs/app")).andExpect(status().isOk()).andReturn();
        String body = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        JsonNode path = objectMapper.readTree(body).at("/paths/~1api~1v1~1app~1businesses~1{id}");
        assertTrue(path.has("get"));
    }
}