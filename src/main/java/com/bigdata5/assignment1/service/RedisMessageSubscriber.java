package com.bigdata5.assignment1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RedisMessageSubscriber implements MessageListener {

    public void onMessage(Message message, byte[] pattern) {

        System.out.println("Message received: " + message.toString());
        JSONObject jsonObject = new JSONObject(message.toString());

        System.out.println(jsonObject);

        ObjectMapper mapper = new ObjectMapper();
        try {
            JsonNode actualObj = mapper.readTree(message.toString());
            System.out.println(actualObj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }


        // Push this JSON Object to indexer of elastic search

    }
}