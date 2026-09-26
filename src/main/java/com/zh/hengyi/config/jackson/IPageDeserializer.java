package com.zh.hengyi.config.jackson;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class IPageDeserializer extends StdDeserializer<IPage<?>> {
    public IPageDeserializer() {
        super(IPage.class);
    }
    @Override
    public IPage<?> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        // json读到之后实际实例化实现类Page
        return p.readValueAs(Page.class);
    }
}
