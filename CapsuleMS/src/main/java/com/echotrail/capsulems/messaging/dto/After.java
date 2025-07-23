package com.echotrail.capsulems.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class After {
    @JsonProperty("event_type")
    private String eventType;
    private EventPayload payload;
}
