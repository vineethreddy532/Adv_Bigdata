package com.bigdata5.assignment1;


import com.bigdata5.assignment1.authorization.JwtTokenUtil;
import com.bigdata5.assignment1.constants.Constants;
import com.bigdata5.assignment1.exceptions.RedisException;
import com.bigdata5.assignment1.service.PlanOps;
import com.bigdata5.assignment1.service.RedisMessagePublisher;
import com.bigdata5.assignment1.service.SchemaOps;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.SignatureException;
import java.util.*;

@RestController
public class Controller {

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    SchemaOps planSchema;

    @Autowired
    PlanOps planOps;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    RedisMessagePublisher redisMessagePublisher;



    @GetMapping(value="/v1/health")
    public HttpStatus process() {
        return HttpStatus.OK;
    }


//##################################   SCHEMA  ---- START   ##############################//
    /**
     * Add Schema to Redis
     * @param mapObj
     * @return
     */
    @RequestMapping(value="/v1/schema", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addSchema(@RequestBody(required = true) Map<String, Object> mapObj,
                                                         @RequestHeader(value = "Authorization", required = true)
                                                                 String bearerAuth
                                                         ){
        String token = bearerAuth.substring(7);

        try {

            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }

        Map<String, Object> savedSchema = planSchema.addSchema(mapObj);
        return  new ResponseEntity<>(savedSchema, HttpStatus.CREATED);
    }

    /**
     * Fetch schema from redis on the basis of name
     * @return
     */
    @RequestMapping(value="/v1/schema", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSchema(@RequestHeader(value = "Authorization", required = true)
                                                                     String bearerAuth) {
        String token = bearerAuth.substring(7);

        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }

        Map<String, Object> fetchedSchema = planSchema.fetchSchema(Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(fetchedSchema, HttpStatus.OK);
    }

    /**
     * Delete schema from redis
     * @return
     */
    @RequestMapping(value="/v1/schema", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteSchema(@RequestHeader(value = "Authorization", required = true)
                                                                        String bearerAuth) throws IOException {


        String token = bearerAuth.substring(7);
        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }


        Map<String, Object> response = planSchema.deleteSchema(Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

//##################################   PLAN  ---- START     ##############################//

    /**
     * Fetch Plan from redis on the basis of name
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan/{planId}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fetchPlan(@PathVariable String planId,
                                                         @RequestHeader(value = "Authorization", required = true)
                                                                        String bearerAuth) {

        String token = bearerAuth.substring(7);
        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is expired", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }

        Map<String, Object> planFetched = planOps.fetchPlan(planId);
        return new ResponseEntity<>(planFetched, HttpStatus.OK);
    }

    /**
     * Add plan
     * @param planObject
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addPlan(@RequestBody(required = true) Map<String, Object> planObject,
                                                       @RequestHeader(value = "Authorization", required = true)
                                                               String bearerAuth) {

        String token = bearerAuth.substring(7);
        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is expired", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }

        Map<String, Object> addPlanResponse =  planOps.addPlan(planObject, Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(addPlanResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Delete PLAN
     * @param planId
     * @return
     */
    @RequestMapping(value="/v1/plan/{planId}", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable String planId,
                                                          @RequestHeader(value = "Authorization", required = true)
                                                                  String bearerAuth) throws IOException {

        String token = bearerAuth.substring(7);
        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is expired", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }


        Map<String, Object> response = planOps.deletePlan(planId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update Plan from redis
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updatePlan(@RequestBody(required = true) Map<String, Object> planObject,
                                                          @RequestHeader(value = "Authorization", required = true)
                                                                  String bearerAuth) {

        String token = bearerAuth.substring(7);
        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is expired", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }

        Map<String, Object> response = planOps.updatePlan(planObject, Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }


    /**
     * PATCH Plan
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> patchPlan(@RequestBody(required = true) Map<String, Object> planObject,
                                                         @RequestHeader(value = "Authorization", required = true)
                                                                 String bearerAuth) throws JsonProcessingException {

        String token = bearerAuth.substring(7);
        try {
            if(!jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME)){
                throw new RedisException("Token is expired", Constants.UNAUTHORIZED);
            }
        } catch (Exception e) {
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }

        ValueOperations valueOperations = redisTemplate.opsForValue();

        Map<String, Object> response = planOps.fetchPlan((String) planObject.get("objectId"));

        List<Map<String,Object>> existingPlanServices = (ArrayList<Map<String, Object>>) response.get("linkedPlanServices");
        List<String> existingPlanServiceIds = (ArrayList<String>) valueOperations.get(planObject.get("objectId") + "_linkedPlanServices");

        // Make schema according to our need
        Map<String, Object> fetchedSchema = planSchema.fetchSchema(Constants.INSURANCE_SCHEMA);
        ((Map<String, Object>) fetchedSchema.get("properties")).remove("planCostShares");
        ((List<String>) fetchedSchema.get("required")).remove("planCostShares");

        // Validate against modified schema
        try {
            JSONObject schemaJSON = new JSONObject(fetchedSchema);
            JSONObject planJson = new JSONObject(planObject);
            Schema schemaValidator = SchemaLoader.load(schemaJSON);
            schemaValidator.validate(planJson);
        } catch (Exception e){
            throw new RedisException("Error due to validation " + e.getMessage() , Constants.BAD_REQUEST);
        }

        // Supports only addition of 1 element to the array per PATCH request
        // Map<String,Object> patchElement = ((ArrayList<Map<String,Object>>)planObject.get("linkedPlanServices")).get(0);

        for(Map<String, Object> patchEle : ((ArrayList<Map<String,Object>>)planObject.get("linkedPlanServices"))) {

            boolean isMatchFound = false;
            for (Map<String,Object> linkedPlan : existingPlanServices){
                if(linkedPlan.get("objectId").equals(patchEle.get("objectId"))) {
                    //Remove existing one and add the provided one in the body
                    existingPlanServices.remove(linkedPlan);
                    existingPlanServices.add(patchEle);
                    isMatchFound = true;
                    break;
                }
            }

            if(!isMatchFound) {
                existingPlanServices.add(patchEle);
                existingPlanServiceIds.add((String) patchEle.get("objectId"));
            }

        }

//        boolean isMatchFound = false;
//        for (Map<String,Object> linkedPlan : existingPlanServices){
//            if(linkedPlan.get("objectId").equals(patchElement.get("objectId"))) {
//                //Remove existing one and add the provided one in the body
//                existingPlanServices.remove(linkedPlan);
//                existingPlanServices.add(patchElement);
//                isMatchFound = true;
//                break;
//            }
//        }
//
//        if(!isMatchFound) {
//            existingPlanServices.add(patchElement);
//            existingPlanServiceIds.add((String) patchElement.get("objectId"));
//        }


        response.put("linkedPlanServices", existingPlanServices);

        valueOperations.set(planObject.get("objectId"), response);

        //Send the response to the RedisMessagePublish
        redisMessagePublisher.publish(response);

        //Send the response to the RedisMessagePublish
        response.put("OPERATION", "patchPlan");
        redisMessagePublisher.publish(response);


        valueOperations.set(planObject.get("objectId") + "_linkedPlanServices" , existingPlanServiceIds);



        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

//##################################   PLAN  ---- END     ##############################//




//##################################   TOKEN GENERATION ---- START     ##############################//

    /**
     * Get Token JWT
     */
    @RequestMapping(value="/v1/token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> processTokenGeneration() {

        String token = jwtTokenUtil.generateToken(Constants.STATIC_USERNAME);
        Map<String, String> k  = new HashMap<>();
        k.put("token", token);
        return new ResponseEntity<>(k, HttpStatus.ACCEPTED);
    }

    /**
     * Validate Token JWT
     */
    @RequestMapping(value="/v1/token", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> processTokenValidation(
                                                          @RequestHeader(value = "Authorization", required = true)
                                                                  String bearerAuth) {
        String token = bearerAuth.substring(7);
        Boolean isTokenValid = false;
        try{
            isTokenValid = jwtTokenUtil.validateToken(token, Constants.STATIC_USERNAME);
        } catch (Exception e){
            throw new RedisException("Token is invalid", Constants.UNAUTHORIZED);
        }
        Map<String, Boolean> k  = new HashMap<>();
        k.put("isTokenValid", isTokenValid);
        return new ResponseEntity<>(k, HttpStatus.OK);
    }


//##################################   TOKEN GENERATION ---- STOP     ##############################//


    @RequestMapping(value="/v1/testsub", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public void processTokenValidation(@RequestBody(required = true) Map<String, Object> planObject) throws JsonProcessingException {

        redisMessagePublisher.publish(planObject);

    }


}
