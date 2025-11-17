package com.transfernow.TransferNow.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.web.util.matcher.IpAddressMatcher;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSecurityInterceptor implements ChannelInterceptor {

    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    private static final List<String> HOST_ONLY_DESTINATIONS = List.of(
            "/app/request/respond"
    );

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && accessor.getCommand() != null) {
            String sessionId = accessor.getSessionId();

            switch (accessor.getCommand()) {
                case CONNECT:
                    String userIdentity = "user-" + sessionId;
                    accessor.setUser(new SimplePrincipal(userIdentity));
                    sessionUserMap.put(sessionId, userIdentity);

                    if (!accessor.getSessionAttributes().containsKey("REMOTE_ADDR")) {
                        accessor.getSessionAttributes().put("REMOTE_ADDR", "unknown");
                    }
                    break;

                case DISCONNECT:
                    sessionUserMap.remove(sessionId);
                    break;

                case SEND:
                    String destination = accessor.getDestination();
                    if (HOST_ONLY_DESTINATIONS.contains(destination)) {
                        String clientIp = accessor.getSessionAttributes().get("REMOTE_ADDR").toString();

                        if (!isLocalhost(clientIp)) {
                            throw new IllegalArgumentException(
                                    "Only the host can send messages to " + destination + ". IP: " + clientIp
                            );
                        }
                    }
                    break;
            }
        }

        return message;
    }

    private boolean isLocalhost(String ip) {
        IpAddressMatcher localhostMatcher = new IpAddressMatcher("127.0.0.1");
        IpAddressMatcher ipv6Matcher = new IpAddressMatcher("::1");
        IpAddressMatcher dockerNet = new IpAddressMatcher("172.16.0.0/12");
        boolean isLocal = ip != null && (localhostMatcher.matches(ip) || ipv6Matcher.matches(ip) || dockerNet.matches(ip));
        return isLocal;
    }

    private static class SimplePrincipal implements Principal {
        private final String name;

        public SimplePrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
