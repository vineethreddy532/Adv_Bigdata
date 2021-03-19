package com.bigdata5.assignment1;


import com.bigdata5.assignment1.authorization.JwtTokenUtil;
import com.bigdata5.assignment1.constants.Constants;
import com.bigdata5.assignment1.exceptions.RedisException;
import com.bigdata5.assignment1.service.PlanOps;
import com.bigdata5.assignment1.service.SchemaOps;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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





    @GetMapping(value="/v1/health")
    public HttpStatus process() {
        return HttpStatus.OK;
    }

    /**
     * Add Schema to Redis
     * @param mapObj
     * @return
     */
    @RequestMapping(value="/v1/schema", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addSchema(@RequestBody(required = true) Map<String, Object> mapObj)
    {

        Map<String, Object> savedSchema = planSchema.addSchema(mapObj);
        return  new ResponseEntity<>(savedSchema, HttpStatus.CREATED);
    }

    /**
     * Fetch schema from redis on the basis of name
     * @return
     */
    @RequestMapping(value="/v1/schema", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> getSchema() {
        Map<String, Object> fetchedSchema = planSchema.fetchSchema(Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(fetchedSchema, HttpStatus.OK);
    }

    @RequestMapping(value="/v1/schema", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deleteSchema() {
        Map<String, Object> response = planSchema.deleteSchema(Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Fetch Plan from redis on the basis of name
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan/{planId}", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> fetchPlan(@PathVariable String planId) {

        Map<String, Object> planFetched = planOps.fetchPlan(planId);
        return new ResponseEntity<>(planFetched, HttpStatus.OK);
    }





    /**
     * Add a plan to the system aganist default schema i.e insurance
     * @param planObject
     * @return
     * @throws Exception
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> addPlan(@RequestBody(required = true) Map<String, Object> planObject)
            throws Exception {
        Map<String, Object> addPlanResponse =  planOps.addPlan(planObject, Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(addPlanResponse, HttpStatus.ACCEPTED);
    }

    /**
     * Delete PLAN
     * @param planId
     * @return
     */
    @RequestMapping(value="/v1/plan/{planId}", method = RequestMethod.DELETE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> deletePlan(@PathVariable String planId) {

        Map<String, Object> response = planOps.deletePlan(planId);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Update Plan from redis
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> updatePlan(@RequestBody(required = true) Map<String, Object> planObject)
    {
        Map<String, Object> response = planOps.updatePlan(planObject, Constants.INSURANCE_SCHEMA);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }


    /**
     * Update Plan from redis
     * @param
     * @return
     */
    @RequestMapping(value="/v1/plan", method = RequestMethod.PATCH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> patchPlan(@RequestBody(required = true) Map<String, Object> planObject)
    {
        Map<String, Object> response = planOps.fetchPlan((String) planObject.get("objectId"));

        Map<String,Object> patchElement = ((ArrayList<Map<String,Object>>)planObject.get("linkedPlanServices")).get(0);

        List<Map<String,Object>> linkedPlans = (List<Map<String, Object>>) response.get("linkedPlanServices");

        boolean isMatchFound = false;
        for (Map<String,Object> linkedPlan : linkedPlans){


            if(linkedPlan.get("objectId").equals(patchElement.get("objectId"))) {
                //Remove existing one and add the provided one in the body
                linkedPlans.remove(linkedPlan);
                linkedPlans.add(patchElement);
                isMatchFound = true;
                break;
            }
        }

        if(!isMatchFound) {
            linkedPlans.add(planObject);
        }


        response.put("linkedPlanServices", linkedPlans);
        ValueOperations valueOperations = redisTemplate.opsForValue();

        valueOperations.set("12xvxc345ssdsds-508", response);

//        JsonNode planArray = (JsonNode) response.get("linkedPlanServices");
//
//        if(planArray.isArray()) {
//            for(JsonNode n : planArray){
//
//            }
//        }


        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

//##################################   TOKEN GENERATION ---- START     ##############################//

    /**
     * Get Token JWT
     */
    @RequestMapping(value="/v1/token", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, String>> processTokenGeneration(@RequestHeader(required = true) String username) {

        String token = jwtTokenUtil.generateToken(username);
        Map<String, String> k  = new HashMap<>();
        k.put("token", token);
        return new ResponseEntity<>(k, HttpStatus.ACCEPTED);
    }

    /**
     * Validate Token JWT
     */
    @RequestMapping(value="/v1/token", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Boolean>> processTokenValidation(@RequestHeader(required = true) String username,
                                                          @RequestHeader(value = "Authorization", required = true)
                                                                  String bearerAuth) {
        String token = bearerAuth.substring(7);
        Boolean isTokenValid = jwtTokenUtil.validateToken(token, username);
        Map<String, Boolean> k  = new HashMap<>();
        k.put("isTokenValid", isTokenValid);
        return new ResponseEntity<>(k, HttpStatus.OK);
    }


//##################################   TOKEN GENERATION ---- STOP     ##############################//



//    /**
//     * Validate Token JWT
//     */
//    @RequestMapping(value="/v1/test", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Boolean> test(@RequestBody(required = true) Map<String, Object> planObject) {
//
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode jsonNodeMap = mapper.convertValue(planObject, JsonNode.class);
//
//        traverse(jsonNodeMap);
//
////        if (jsonNodeMap.isObject()) {
////            Iterator<String> fieldNames = jsonNodeMap.fieldNames();
////
////            for (Iterator<String> it = fieldNames; it.hasNext(); ) {
////                String s = it.next();
////                System.out.println(s);
////            }
////        }
//
//        return new ResponseEntity<>(true, HttpStatus.OK);
//    }
//
//    public void traverse(JsonNode root){
//
//        Map<String, Object> simple = new HashMap<>();
//
//        if(root.isObject()) {
//            Iterator<String> fieldNames = root.fieldNames();
//
//            while(fieldNames.hasNext()) {
//
//                String fieldName = fieldNames.next();
//                JsonNode fieldValue = root.get(fieldName);
//
//                //Collect all simple key and values
//                if(!fieldValue.isObject() && !fieldValue.isArray()) {
//                    simple.put(fieldName, fieldValue);
//                }
//                // traverse(fieldValue);
//            }
//
//            //Add to redis the key and above map
//            ValueOperations valueOperations = redisTemplate.opsForValue();
//
//            valueOperations.set(root.get("objectId").toString() , simple);
//
//        } else if(root.isArray()){
////            ArrayNode arrayNode = (ArrayNode) root;
////            for(int i = 0; i < arrayNode.size(); i++) {
////                JsonNode arrayElement = arrayNode.get(i);
////                traverse(arrayElement);
////            }
//        } else {
//            // JsonNode root represents a single value field - do something with it.
//
//        }
//    }
//
//
//    @RequestMapping(value="/v1/plan/added", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<Map<String, Object>> postAPlanForPatch(@RequestBody(required = true) Map<String, Object> plan)
//            throws Exception {
//
//        ValueOperations valueOperations = redisTemplate.opsForValue();
//
//        String planIdMain = (String) plan.get("objectId");
//
//        //For validation of plan only
//        Map<String, Object> fetchedSchema = planSchema.fetchSchema("insurance");
//        JSONObject planJson = new JSONObject(plan);
//        JSONObject schemaJSON = new JSONObject(fetchedSchema);
//
//        try{
//            // Validate Json is according to the Schema Rules
//            org.everit.json.schema.Schema schemaValidator = SchemaLoader.load(schemaJSON);
//            schemaValidator.validate(planJson);
//
//            // Get jsonnode of plan
//            ObjectMapper mapper = new ObjectMapper();
//            JsonNode planNode = mapper.convertValue(plan, JsonNode.class);
//
//            /**
//             * As schema is been validated it is obvious that the body is object
//             */
//            //if (planNode.isObject()){
//            //  Iterator<String> planFeildNames = planNode.fieldNames();
//            //}
//            Iterator<String> planFieldNames = planNode.fieldNames();
//
//            Map<String,Object> planSimpleKeys = new  HashMap<>();
//
//            Map<String,Object> planCostShareKeys = new  HashMap<>();
//
//            //Fetch all simple Keys and save in the Database as "508-111"
//            while (planFieldNames.hasNext()) {
//
//                String fieldName = planFieldNames.next();
//
//                //Simple Values in Main Object
//                if(planNode.get(fieldName).isValueNode()){
//                    planSimpleKeys.put(fieldName, planNode.get(fieldName).textValue());
//                }
//
//                // planCostShares
//                else if(planNode.get(fieldName).isObject()){
//                    System.out.println("this is plan cost shares");
//
//                    //Fetch PlanCostShare Object and convert to JSOn
//                    JsonNode planCostShare = planNode.get(fieldName);
//                        Map<String, Object> simpleKeys = mapper.convertValue(planCostShare, new TypeReference<Map<String, Object>>(){});
//
//                    //Create a new key-value with Key as planIdMain-objectId and value as map of it's simple pair
//                    String mixed_key_plancostshares = planIdMain + "-planCostShares";
//                    valueOperations.set(mixed_key_plancostshares, simpleKeys);
//
//                 // linkedPlanServices
//                } else if(planNode.get(fieldName).isArray()){
//
//                    System.out.println("this is linkedinPlanService");
//
//                    JsonNode linkedPlanServices = planNode.get(fieldName);
//                    int i = 0;
//                    for (JsonNode n : linkedPlanServices){
//
//                        System.out.println(n.get("objectId"));
//                        valueOperations.set("linkedPlanServices_" + i +  "_" + n.get("objectId").textValue(), n );
//                        i++;
//                    }
//                }
//            }
//            //As of now, only simple values are there
//            valueOperations.set(planIdMain, planSimpleKeys);
//
//
//
//            // ******   GET TEsting *****
//
//            Map<String, Object> planFetchedGET = (Map<String, Object>) valueOperations.get("12xvxc345ssdsds-508");
//
//            Map<String, Object> planCostSharesGET = (Map<String, Object>) valueOperations.get("12xvxc345ssdsds-508-planCostShares");
//
//            planFetchedGET.put("planCostShares", planCostSharesGET);
//
//            //Get linked plan services
//
//            Object linkedPlanServiceGET_0 =  valueOperations.get("linkedPlanServices_0_27283xvx9sdf-507").toString();
//            Object linkedPlanServiceGET_1 =  valueOperations.get("linkedPlanServices_1_27283xvx9sdf-507").toString();
//
//            List<Object> planLinkedServices = new ArrayList<>();
//
//            planLinkedServices.add(linkedPlanServiceGET_0);
//            planLinkedServices.add(linkedPlanServiceGET_1);
//
//            planFetchedGET.put("linkedPlanServices", planLinkedServices);
//
//            return new ResponseEntity<>(planFetchedGET, HttpStatus.ACCEPTED);
//
//        } catch (IllegalArgumentException e ){
//            Map<String, Object> k  = new HashMap<>();
//            k.put("message", "Plan was not present so Updated successfully");
//            return new ResponseEntity<>(k, HttpStatus.ACCEPTED);
//        } catch (Exception e ){
//            // throw new RedisException("Error due to validation/unknown issue", Constants.BAD_REQUEST);
//
//            throw new Exception(e);
//        }
//    }

}
