package com.bigdata5.assignment1;


import com.bigdata5.assignment1.constants.Constants;
import com.bigdata5.assignment1.exceptions.RedisException;
import com.bigdata5.assignment1.service.PlanSchemaImpl;
import com.fasterxml.jackson.databind.JsonNode;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class Controller {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    PlanSchemaImpl planSchema;

    @PostMapping(value="/process")
    public JsonNode process(@RequestBody com.fasterxml.jackson.databind.JsonNode payload) {
        System.out.println(payload);
        return payload;
    }


    /**
     * Add Schema to Redis
     * @param mapObj
     * @param name
     * @return
     */
    @RequestMapping(value="/v1/schema", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> process(@RequestBody(required = true) Map<String, Object> mapObj,
                                                       @RequestHeader(required = false) String name) {

        Map<String, Object> savedSchema = planSchema.addSchema(mapObj, name);
        return  new ResponseEntity<>(savedSchema, HttpStatus.CREATED);
    }

    /**
     * Fetch schema from redis on the basis of name
     * @param name
     * @return
     */
    @RequestMapping(value="/v1/schema/{name}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> process(@PathVariable String name) {

        Map<String, Object> fetchedSchema = planSchema.fetchSchema(name);
        return new ResponseEntity<>(fetchedSchema, HttpStatus.OK);
    }

    @RequestMapping(value="/v1/plan/add", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addPlan(@RequestHeader(defaultValue = "insurance", required = false) String schema,
                                                       @RequestBody(required = true) Map<String, Object> planObject) throws Exception {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        //Fetch Schema and see if it exists, if not throw an error
        Map<String, Object> fetchedSchema = (Map<String, Object>) valueOperations.get(schema);
        if(fetchedSchema == null) {
            throw new RedisException("Schema doesn't exist in the system, so can't add plan aganist this schema.", "Not Found");
        }

        if(null != (Map<String, Object>) valueOperations.get(planObject.get("objectId"))) {
            throw new RedisException("Plan already existis in the system", Constants.BAD_REQUEST);
        }

        try {

            JSONObject schemaJSON = new JSONObject(fetchedSchema);
            JSONObject planJson = new JSONObject(planObject);

            org.everit.json.schema.Schema schemaValidator = SchemaLoader.load(schemaJSON);
            schemaValidator.validate(planJson);

            valueOperations.set(planObject.get("objectId"), planObject);
            Map<String, Object> k  = new HashMap<>();
            k.put("message", "Plan added successfully");
            return new ResponseEntity<>(k, HttpStatus.ACCEPTED);

        } catch (Exception e) {
            throw new RedisException("Error in validating the template!", Constants.BAD_REQUEST);
        }
    }

    /**
     * Fetch Plan from redis on the basis of name
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan/{objectId}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fetchPlan(@PathVariable String objectId) {

        ValueOperations valueOperations = redisTemplate.opsForValue();

        Map<String, Object> planFetched = (Map<String, Object>) valueOperations.get(objectId);

        if(null == planFetched) {
            throw new RedisException("Plan doesn't exist in the system", Constants.NOT_FOUND);
        }

        return new ResponseEntity<>(planFetched, HttpStatus.OK);
    }


    /**
     * Update Plan from redis
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updatePlan(@RequestHeader(defaultValue = "insurance", required = false) String schema,
                                                          @RequestBody(required = true) Map<String, Object> planObject)
            throws Exception {

        ValueOperations valueOperations = redisTemplate.opsForValue();

        String planProvidedInbody = (String) planObject.get("objectId");
        Map<String, Object> fetchedSchema = planSchema.fetchSchema("insurance");
        //Validate the body

        JSONObject planJson = new JSONObject(planObject);
        JSONObject schemaJSON = new JSONObject(fetchedSchema);

        try{
            org.everit.json.schema.Schema schemaValidator = SchemaLoader.load(schemaJSON);
            schemaValidator.validate(planJson);

            Map<String, Object> planFetched = (Map<String, Object>) valueOperations.get(planProvidedInbody);

            valueOperations.set(planProvidedInbody, planObject);
            Map<String, Object> k  = new HashMap<>();
            k.put("message", "Plan is Updated successfully");
            return new ResponseEntity<>(k, HttpStatus.ACCEPTED);

        } catch (IllegalArgumentException e ){
            valueOperations.set(planObject.get("objectId"), planObject);
            Map<String, Object> k  = new HashMap<>();
            k.put("message", "Plan was not present so Updated successfully");
            return new ResponseEntity<>(k, HttpStatus.ACCEPTED);
        } catch (Exception e ){
            throw new RedisException("Error due to validation/unknown issue", Constants.BAD_REQUEST);
        }
    }
}
