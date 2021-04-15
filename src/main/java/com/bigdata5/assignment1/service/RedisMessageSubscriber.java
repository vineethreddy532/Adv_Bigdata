package com.bigdata5.assignment1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.org.apache.xerces.internal.xs.datatypes.ObjectList;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.json.JSONObject;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
public class RedisMessageSubscriber implements MessageListener {


    public void onMessage(Message message, byte[] pattern) {

        System.out.println("Message received: " + message.toString());
        // JSONObject jsonObject = new JSONObject(message.toString());

        HashMap<Object, Object> jsonObject = null;
        try {
            jsonObject = new ObjectMapper().readValue(message.toString(), HashMap.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));


        //Fetch the simple fields first and save them to database

        if("addPlan".equalsIgnoreCase((String) jsonObject.get("OPERATION"))) {
            try {
                putParentChildInElastic(jsonObject, client);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    private void putParentChildInElastic(HashMap<Object, Object> jsonObject, RestHighLevelClient client) throws JsonProcessingException {

    // Plan -> No_Child
        Map<String, Object> parentMap = new HashMap<>();
        parentMap.put("_org", jsonObject.get("_org"));
        parentMap.put("objectId", jsonObject.get("objectId"));
        parentMap.put("objectType", jsonObject.get("objectType"));
        parentMap.put("planType", jsonObject.get("planType"));
        parentMap.put("creationDate", jsonObject.get("creationDate"));

        Map<String, Object> childMap = new HashMap<>();
        childMap.put("name", "plan");
        parentMap.put("plan_join", childMap);

        IndexRequest indexRequest = new IndexRequest("plan")
                .id((String) jsonObject.get("objectId")).source(parentMap);
        try {
            client.index(indexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            System.out.println(e);
        }

        //Clear the maps
        parentMap.clear();
        childMap.clear();

    // Plan -> MemberCostShare

        //HashMap<String,Object> planCostShares = new ObjectMapper().readValue(jsonObject.get("planCostShares").toString(), HashMap.class);
        HashMap<String,Object> planCostShares = (HashMap<String, Object>) jsonObject.get("planCostShares");
        parentMap.put("_org", planCostShares.get("_org"));
        parentMap.put("objectId", planCostShares.get("objectId"));
        parentMap.put("objectType", planCostShares.get("objectType"));
        parentMap.put("copay", planCostShares.get("copay"));
        parentMap.put("deductible", planCostShares.get("deductible"));

        childMap.put("name", "membercostshare"); // May be need to change this
        childMap.put("parent", jsonObject.get("objectId"));
        parentMap.put("plan_join", childMap);

        IndexRequest planCostSharesIndexRequest= new IndexRequest("plan")
                .id((String) planCostShares.get("objectId")).source(parentMap);
        planCostSharesIndexRequest.routing("1");
        try {
            client.index(planCostSharesIndexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            System.out.println(e);
        }
        //Clear the maps
        parentMap.clear();
        childMap.clear();


 //Iterate over the linkedPlanServices and map

        // Object o = jsonObject.get("linkedPlanServices");

        List<Object> linkedPlanServices = (ArrayList<Object>) jsonObject.get("linkedPlanServices");

        linkedPlanServices.remove(0);
        // List<Map<String, Object>> linkedPlanServices = new ObjectMapper().readValue(jsonObject.get("linkedPlanServices").toString(), ArrayList.class);

        List<Map<Object, Object>> modifiedLinkedPlanServices = new ArrayList<>();

        List<Object> lps = (ArrayList<Object>) linkedPlanServices.get(0);

        for (Object linkedPlanService : lps) {
            modifiedLinkedPlanServices.add((Map<Object, Object>) linkedPlanService);
        }

        for (Map<Object, Object> linkedPlanService : modifiedLinkedPlanServices) {

        // Plan -> PlanService
            parentMap.put("_org", linkedPlanService.get("_org"));
            parentMap.put("objectId", linkedPlanService.get("objectId"));
            parentMap.put("objectType", linkedPlanService.get("objectType"));

            childMap.put("name", "planservice"); // May be need to change this
            childMap.put("parent", jsonObject.get("objectId"));

            parentMap.put("plan_join", childMap);

            IndexRequest planServiceIndexRequest= new IndexRequest("plan")
                    .id((String) linkedPlanService.get("objectId")).source(parentMap);
            planServiceIndexRequest.routing("1");
            try {
                client.index(planServiceIndexRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                System.out.println(e);
            }
            //Clear maps
            parentMap.clear();
            childMap.clear();

        // PlanService -> service

            Map<String, Object> linkedService = (Map<String, Object>) linkedPlanService.get("linkedService");
            // HashMap<String,Object> linkedService = new ObjectMapper().readValue(linkedPlanService.get("linkedService").toString(), HashMap.class);

            parentMap.put("_org", linkedService.get("_org"));
            parentMap.put("objectId", linkedService.get("objectId"));
            parentMap.put("objectType", linkedService.get("objectType"));
            parentMap.put("name", linkedService.get("name"));

            childMap.put("name", "service"); // May be need to change this
            childMap.put("parent", linkedPlanService.get("objectId"));
            parentMap.put("plan_join", childMap);

            IndexRequest linkedServiceIndexRequest= new IndexRequest("plan")
                    .id((String) linkedService.get("objectId")).source(parentMap);
            linkedServiceIndexRequest.routing("1");
            try {
                client.index(linkedServiceIndexRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                System.out.println(e);
            }
            //Clear maps
            parentMap.clear();
            childMap.clear();


        // PlanService -> membercostshare_copy

            // HashMap<String,Object> planserviceCostShares = new ObjectMapper().readValue(linkedPlanService.get("planserviceCostShares").toString(), HashMap.class);
            Map<String, Object> planserviceCostShares = (Map<String, Object>) linkedPlanService.get("planserviceCostShares");

            parentMap.put("_org", planserviceCostShares.get("_org"));
            parentMap.put("objectId", planserviceCostShares.get("objectId"));
            parentMap.put("objectType", planserviceCostShares.get("objectType"));
            parentMap.put("copay", planserviceCostShares.get("copay"));
            parentMap.put("deductible", planserviceCostShares.get("deductible"));

            childMap.put("name", "membercostshare_copy"); // May be need to change this
            childMap.put("parent", linkedPlanService.get("objectId"));

            parentMap.put("plan_join", childMap);

            IndexRequest planserviceCostSharesIndexRequest= new IndexRequest("plan")
                    .id((String) planserviceCostShares.get("objectId")).source(parentMap);
            planserviceCostSharesIndexRequest.routing("1");
            try {
                client.index(planserviceCostSharesIndexRequest, RequestOptions.DEFAULT);
            } catch (Exception e) {
                System.out.println(e);
            }

            parentMap.clear();
            childMap.clear();
        }
    }
}