package com.vyoog.eisplatform.modules.toolsync.mcp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.tool.execution.ToolCallResultConverter;

import java.lang.reflect.Type;

/** A tool's result object (contract v1 section 6) written as JSON, nulls left out. Spring AI's default converter does not follow the app's Jackson settings. */
public class ToolMcpResultConverter implements ToolCallResultConverter {

    private static final ObjectMapper MAPPER = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

    @Override
    public String convert(Object result, Type returnType) {
        try {
            return MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not write the tool result as JSON", e);
        }
    }
}
