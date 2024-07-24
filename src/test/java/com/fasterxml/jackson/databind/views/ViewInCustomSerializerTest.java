package com.fasterxml.jackson.databind.views;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.testutil.DatabindTestUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ViewInCustomSerializerTest extends DatabindTestUtil {

    static class ViewA {
    }

    static class ViewB {
    }

    @JsonSerialize(using = ViewInCustomSerializerTest.ParentBeanSerializer.class)
    static class ParentBean {
        public Bean bean = new Bean();
    }

    static class Bean {

        @JsonView(ViewA.class)
        public String a = "1";
        @JsonView(ViewB.class)
        public String b = "2";
    }

    static class ParentBeanSerializer extends JsonSerializer<ParentBean> {

        @Override
        public void serialize(ParentBean value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            gen.writeFieldName("customName");
            // serializers.findValueSerializer(Bean.class).serialize(value.bean, gen, serializers);
            gen.writePOJO(value.bean);
            gen.writeEndObject();
        }
    }

    private final ObjectMapper MAPPER = newJsonMapper();

    @Test
    public void testWithNoViewDefined() throws IOException {

        ViewInCustomSerializerTest.ParentBean bean = new ViewInCustomSerializerTest.ParentBean();

        // no view defined => everything is serialized
        String json = MAPPER.writeValueAsString(bean);
        Map<String, Map<String, String>> map = MAPPER.readValue(json, Map.class);
        assertNull(map.get("bean"));
        assertEquals("1", (map.get("customName")).get("a"));
        assertEquals("2", (map.get("customName")).get("b"));
    }

    @Test
    public void testWithViewADefined() throws IOException {

        ViewInCustomSerializerTest.ParentBean bean = new ViewInCustomSerializerTest.ParentBean();
        // viewA defined => everything is serialized
        String json = MAPPER.writerWithView(ViewA.class).writeValueAsString(bean);
        Map<String, Map<String, String>> map = MAPPER.readValue(json, Map.class);
        assertNull(map.get("bean"));
        assertEquals("1", (map.get("customName")).get("a"));
        assertEquals(null, (map.get("customName")).get("b"));
    }

    @Test
    public void testWithViewBDefined() throws IOException {
        ViewInCustomSerializerTest.ParentBean bean = new ViewInCustomSerializerTest.ParentBean();
        // viewB defined => only b should be serialized
        String json = MAPPER.writerWithView(ViewB.class).writeValueAsString(bean);
        Map<String, Map<String, String>> map = MAPPER.readValue(json, Map.class);
        assertNull(map.get("bean"));
        assertEquals(null, (map.get("customName")).get("a"));
        assertEquals("2", (map.get("customName")).get("b"));
    }

}
