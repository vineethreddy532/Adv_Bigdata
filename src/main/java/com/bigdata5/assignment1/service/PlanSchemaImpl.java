package com.bigdata5.assignment1.service;

import com.bigdata5.assignment1.constants.Constants;
import com.bigdata5.assignment1.exceptions.RedisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PlanSchemaImpl implements SchemaOps {

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> addSchema(Map<String, Object> schemaObject) {

        ValueOperations valueOperations = redisTemplate.opsForValue();

        try {
            valueOperations.set(Constants.INSURANCE_SCHEMA, schemaObject);
            return schemaObject;
        } catch (Exception e){
            throw new RedisException(e.getMessage(), Constants.BAD_REQUEST);
        }
    }


    @Override
    public Map<String, Object> fetchSchema(String schemaName) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        try {
            Map<String, Object> fetchedSchema = (Map<String, Object>) valueOperations.get(schemaName);
            if(null == fetchedSchema) {
                throw new RedisException("Schema doesn't exist in the system", Constants.NOT_FOUND);
            }
            return fetchedSchema;
        } catch (Exception e){
            throw new RedisException(e.getMessage(), Constants.NOT_FOUND);
        }
    }

    @Override
    public Map<String, Object> deleteSchema(String schemaName) {
        ValueOperations valueOperations = redisTemplate.opsForValue();

        valueOperations.set(schemaName, null);
        Map<String, Object> response  = new HashMap<>();
        response.put("message", "Schema deleted successfully");
        return response;
    }
}

