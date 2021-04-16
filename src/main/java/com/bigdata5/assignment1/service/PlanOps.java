package com.bigdata5.assignment1.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public interface PlanOps {

    default Map<String, Object> addPlan(Map<String, Object> plan, String schema) {
        return new HashMap<>();
    }

    default Map<String, Object> fetchPlan(String planId) {
        return new HashMap<>();
    }

    default Map<String, Object> deletePlan(String planId) throws IOException {
        return new HashMap<>();
    }

    default Map<String, Object> updatePlan(Map<String, Object> plan, String schema)
    {
        return new HashMap<>();
    }

    default Map<String, Object> updateLinkedPlanServices(Map<String, Object> plan, String schema)
    {
        return new HashMap<>();
    }
}
