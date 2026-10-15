package org.yht.exps.express.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.yht.exps.express.model.Notification;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class NotificationController {

	@Autowired
	SimpMessageSendingOperations messagingTemplate;
	
	@MessageMapping(value="{userId}")
    public void message(
    		@DestinationVariable("userId") String userId,
    		@Payload Notification notification) throws Exception {
        messagingTemplate.convertAndSend("/sub/" + userId, notification);
    }
}
