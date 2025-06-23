package com.echotrail.friendshipms.DTO;

import lombok.Data;

@Data
public class FriendshipResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String status;
    private String createdAt;
}

