package com.bigdata5.assignment1.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public interface SchemaOps {

    default Map<String, Object> addSchema(Map<String, Object> schema){
        return new HashMap<>();
    };

    default Map<String, Object> fetchSchema(String schemaName){
        return new HashMap<>();
    };

    default Map<String, Object> deleteSchema(String schemaName) throws IOException {
        return new HashMap<>();
    };

}
