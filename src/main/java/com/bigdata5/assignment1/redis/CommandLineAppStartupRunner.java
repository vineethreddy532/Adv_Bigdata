//package com.bigdata5.assignment1.redis;
//
//import org.apache.http.HttpHost;
//import org.elasticsearch.client.RequestOptions;
//import org.elasticsearch.client.RestClient;
//import org.elasticsearch.client.RestHighLevelClient;
//import org.elasticsearch.client.indices.CreateIndexRequest;
//import org.elasticsearch.common.xcontent.XContentType;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Component;
//
//
//@Component
//public class CommandLineAppStartupRunner implements CommandLineRunner {
//    private static final Logger LOG =
//            LoggerFactory.getLogger(CommandLineAppStartupRunner.class);
//
//    public static int counter;
//
//    @Override
//    public void run(String...args) throws Exception {
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
//        RestHighLevelClient client2 = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http")));
//
//        CreateIndexRequest request = new CreateIndexRequest("plan");
//        request.mapping(map, XContentType.JSON);
//        client2.indices().create(request, RequestOptions.DEFAULT);
//        System.out.println("Plan mapping is created!!!");
//    }
//}
