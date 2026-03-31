package org.codenot.househub.advisor.service.knowledgemanagement;

import java.util.Arrays;

public enum Topic {
    JAVA, QUANTUM, TAX, FINANCE;

    public static Topic fromString(String topic) {
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(topic))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid topic: " + topic + ". This might come from the inconsistency of the response from Claude."));
    }
}
