package com.bigdata5.assignment1.service;

import com.bigdata5.assignment1.constants.Constants;
import com.bigdata5.assignment1.exceptions.RedisException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PlanImpl implements PlanOps {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SchemaOps schemaOps;

    @Autowired
    RedisMessagePublisher redisMessagePublisher;

    @Override
    public Map<String, Object> addPlan(Map<String, Object> plan, String schema) {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Map<String, Object> fetchedSchema = schemaOps.fetchSchema(schema);

        if(null != (Map<String, Object>) valueOperations.get(plan.get("objectId"))) {
            throw new RedisException("Plan already exists in the system", Constants.BAD_REQUEST);
        }
        try {

            //Validate aganist schema and save to DB
            JSONObject schemaJSON = new JSONObject(fetchedSchema);
            JSONObject planJson = new JSONObject(plan);

            Schema schemaValidator = SchemaLoader.load(schemaJSON);
            schemaValidator.validate(planJson);

            //Validation Success
            List<Map<String, Object>> linkedPlanServices = (ArrayList<Map<String, Object>>) plan.get("linkedPlanServices");

            List<String> linkedPlanServicesIds = new ArrayList<>();

            for (Map<String, Object> linkedPlanService : linkedPlanServices) {
                linkedPlanServicesIds.add((String) linkedPlanService.get("objectId"));
            }

            String keyForlinkedPlanServicesIds = plan.get("objectId") + "_linkedPlanServices";

            saveToRedis(keyForlinkedPlanServicesIds, linkedPlanServicesIds);
            saveToRedis((String) plan.get("objectId"), plan);

            // Publish whole plan to redis Publisher
            plan.put("OPERATION", "addPlan");
            redisMessagePublisher.publish(plan);


            Map<String, Object> successResponse  = new HashMap<>();
            successResponse.put("message", "Plan added successfully");
            return successResponse;

        } catch (Exception e) {
            throw new RedisException("Error in validating the template!", Constants.BAD_REQUEST);
        }
    }

    @Override
    public Map<String, Object> fetchPlan(String planId) {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Map<String, Object> planFetched = (Map<String, Object>) valueOperations.get(planId);
        if(null == planFetched) {
            throw new RedisException("Plan doesn't exist in the system", Constants.NOT_FOUND);
        }
        return planFetched;
    }

    @Override
    public Map<String, Object> updatePlan(Map<String, Object> plan, String schema) {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        Map<String, Object> fetchedSchema = schemaOps.fetchSchema(schema);
        JSONObject planJson = new JSONObject(plan);
        JSONObject schemaJSON = new JSONObject(fetchedSchema);

        try{
            //Validate and add to DB
            Schema schemaValidator = SchemaLoader.load(schemaJSON);
            schemaValidator.validate(planJson);

            List<Map<String, Object>> linkedPlanServices = (ArrayList<Map<String, Object>>) plan.get("linkedPlanServices");
            List<String> linkedPlanServicesIds = new ArrayList<>();
            for (Map<String, Object> linkedPlanService : linkedPlanServices) {
                linkedPlanServicesIds.add((String) linkedPlanService.get("objectId"));
            }

            valueOperations.set(plan.get("objectId") + "_linkedPlanServices", linkedPlanServicesIds);
            valueOperations.set(plan.get("objectId"), plan);

            Map<String, Object> k  = new HashMap<>();
            k.put("message", "Plan is Updated successfully");
            return k;

        } catch (Exception e ){
            throw new RedisException("Error due to validation/unknown issue: " + e.getMessage() , Constants.BAD_REQUEST);
        }
    }

    @Override
    public Map<String, Object> deletePlan(String planId) {

        //Set the list of linkedPlanServices to null as we dont have the plan for this
        saveToRedis(planId + "_linkedPlanServices" , null);
        saveToRedis(planId, null);

        Map<String, Object> response  = new HashMap<>();
        response.put("message", "Plan deleted successfully");
        return response;
    }


    private void saveToRedis(String key, Object value) {

        ValueOperations valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);

    }
}
