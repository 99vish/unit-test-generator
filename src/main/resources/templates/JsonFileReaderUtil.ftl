package com.blumeglobal;

import com.blumeglobal.platform.core.common.AbstractBizOperation;
import com.blumeglobal.platform.core.common.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonFileReaderUtil {

    public static <T> List<T> readRequestEntities(String pathToJsonFile, Class<T> targetClass) throws IOException
    {
        Path path = Paths.get(pathToJsonFile);
        byte[] bytes = Files.readAllBytes(path);

        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, targetClass);
            return objectMapper.readValue(new String(bytes), listType);
        } catch (IOException e) {
            throw new IOException("Error reading file, Please check file name and specified path " + e);
        }
    }

    public static <T extends AbstractBizOperation> ApiResponse<T> readResponseEntities(String pathToJsonFile, Class<T> targetClass) throws Exception {
        Path path = Paths.get(pathToJsonFile);
        byte[] bytes = Files.readAllBytes(path);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(bytes);


        ArrayNode resultsNode = (ArrayNode) jsonNode.get("results");

        List<T> resultList = new ArrayList<>();
        for (JsonNode node : resultsNode) {
            T object = objectMapper.treeToValue(node, targetClass);
            resultList.add(object);
        }

        ((ObjectNode) jsonNode).remove("results");

        // Deserialize the remaining JSON into the ApiResponse object
        ApiResponse<T> apiResponse = objectMapper.readValue(jsonNode.toString(), new TypeReference<ApiResponse<T>>() {});

        // Set the results list
        apiResponse.setResults(resultList);

        return apiResponse;
    }
}

