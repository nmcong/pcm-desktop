package com.noteflix.pcm.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * Function call request from LLM
 * <p>
 * When the LLM decides to call a function, it returns this object
 * containing the function name and arguments (as JSON string)
 * <p>
 * Example:
 * <pre>
 * FunctionCall call = FunctionCall.builder()
 *     .name("get_weather")
 *     .arguments("{\"location\": \"Tokyo\", \"unit\": \"celsius\"}")
 *     .build();
 * </pre>
 *
 * @author PCM Team
 * @version 1.0.0
 */
@Data
@Builder
public class FunctionCall {

    /**
     * Name of the function to call
     * Example: "get_weather"
     */
    private String name;

    /**
     * Function arguments as JSON string
     * <p>
     * Example: "{\"location\": \"Tokyo\", \"unit\": \"celsius\"}"
     * <p>
     * Parse this JSON to get the actual arguments:
     * <pre>
     * ObjectMapper mapper = new ObjectMapper();
     * Map<String, Object> args = mapper.readValue(call.getArguments(), Map.class);
     * String location = (String) args.get("location");
     * </pre>
     */
    private String arguments;

    /**
     * Parse arguments to a Java object
     *
     * @param clazz Target class
     * @param <T>   Type
     * @return Parsed object
     * @throws com.fasterxml.jackson.core.JsonProcessingException if parsing fails
     */
    public <T> T parseArguments(Class<T> clazz) throws com.fasterxml.jackson.core.JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(arguments, clazz);
    }

    /**
     * Parse arguments to a Map
     *
     * @return Arguments as Map
     * @throws com.fasterxml.jackson.core.JsonProcessingException if parsing fails
     */
    @SuppressWarnings("unchecked")
    public java.util.Map<String, Object> parseArgumentsAsMap() throws com.fasterxml.jackson.core.JsonProcessingException {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        return mapper.readValue(arguments, java.util.Map.class);
    }
}

