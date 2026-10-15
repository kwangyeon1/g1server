package org.yht.config;

import java.util.Objects;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import org.yht.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class FilterChannelInterceptor implements ChannelInterceptor {
	
	private final JwtTokenProvider jwtTokenProvider;
	
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
 
        if (StompCommand.CONNECT.equals(headerAccessor.getCommand())) {
            if (!jwtTokenProvider.validateToken(Objects.requireNonNull(headerAccessor.getFirstNativeHeader("Authorization")).substring(7))) {
            	throw new MessagingException("no permission! ");
            }
        }
        
        return message;
    }
}