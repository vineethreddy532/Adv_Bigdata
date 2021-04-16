//package com.bigdata5.assignment1;
//
//import org.apache.http.HttpHost;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.client.indices.CreateIndexResponse;
//import org.elasticsearch.common.xcontent.XContentType;
//
//import java.io.IOException;
//
//public class Test {
//
//
//    public static void main(String[] args) throws IOException {
//
//        String map = "{\r\n" +
//                "    \"properties\": {\r\n" +
//                "      \"objectId\": {\r\n" +
//                "        \"type\": \"keyword\"\r\n" +
//                "      },\r\n" +
//                "      \"plan_service\":{\r\n" +
//                "        \"type\": \"join\",\r\n" +
//                "        \"relations\":{\r\n" +
//                "          \"plan\": [\"membercostshare\", \"planservice\"],\r\n" +
//                "          \"planservice\": [\"service\", \"membercostshare_copy\"]\r\n" +
//                "        }\r\n" +
//                "      }\r\n" +
//                "    }\r\n" +
//                "  }\r\n" +
//                "}";
//
//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http")));
//
//        CreateIndexRequest request = new CreateIndexRequest("plan");
//        request.mapping(map, XContentType.JSON);
//        CreateIndexResponse createIndexResponse = client.indices().create(request, RequestOptions.DEFAULT);
//

//
//
//
//    }
//}


//        System.out.println(jsonObject);
//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            JsonNode actualObj = mapper.readTree(message.toString());
//            System.out.println(actualObj);
//        } catch (JsonProcessingException e) {
//            e.printStackTrace();
//        }
//        // Push this JSON Object to indexer of elastic search

//LinkedHashMap<String, Object> planCostShares = (LinkedHashMap<String, Object>) jsonObject.get("planCostShares");
//try{
//        HashMap<String,Object> planCostShares = new ObjectMapper().readValue(jsonObject.get("planCostShares").toString(), HashMap.class);
//        parentMap.put("_org", result.get("_org"));
////            LinkedHashMap<String, Object> planCostShares = (LinkedHashMap<String, Object>) jsonObject.get("planCostShares");
//        } catch (Exception e) {
//        System.out.println(e);
//
//        }