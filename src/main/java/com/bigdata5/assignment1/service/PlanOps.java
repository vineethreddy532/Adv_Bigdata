package com.bigdata5.assignment1.service;

import java.util.HashMap;
import java.util.Map;

public interface PlanOps {

    default Map<String, Object> addPlan(Map<String, Object> plan, String schema) {
        return new HashMap<>();
    }

    default Map<String, Object> fetchPlan(String planName, String schema) {
        return new HashMap<>();
    }

    default void deletePlan(String planName, String schema) {
        return ;
    }

}
