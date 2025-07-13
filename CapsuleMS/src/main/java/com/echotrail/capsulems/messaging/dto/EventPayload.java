package com.echotrail.capsulems.messaging.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventPayload {
    private long id;
    private long userId;
    private boolean chained;
}
