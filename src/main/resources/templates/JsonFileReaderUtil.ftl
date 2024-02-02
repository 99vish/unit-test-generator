package com.blumeglobal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonFileReaderUtil {

    public static <T> List<T> readEntities(String pathToJsonFile, Class<T> targetClass) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        try
        {
            CollectionType listType = objectMapper.getTypeFactory()
                    .constructCollectionType(ArrayList.class, targetClass);
            return objectMapper.readValue(pathToJsonFile, listType);
        } catch (IOException e) {
            throw new IOException("Error reading file, Please check file name and specified path " + e);
        }
    }
}