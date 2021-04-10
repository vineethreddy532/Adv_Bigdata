package com.bigdata5.assignment1.service;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Map;

public interface MessagePublisher {
    void publish(Map<String, Object> message) throws JsonProcessingException;
}
