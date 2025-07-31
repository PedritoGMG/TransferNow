package com.transfernow.TransferNow.config;
import org.bitlet.weupnp.GatewayDevice;
import org.bitlet.weupnp.GatewayDiscover;
import org.bitlet.weupnp.PortMappingEntry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.net.InetAddress;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "upnp.enabled", havingValue = "true")
public class WeUPnPPortMapper implements DisposableBean {

    private GatewayDevice activeGateway;
    private final int externalPort = 8080;
    private final int internalPort = 8080;
    private final String protocol = "TCP";
    private final String mappingDescription = "Spring Boot App";

    @EventListener(ApplicationReadyEvent.class)
    public void openPortOnStartup() {
        try {
            GatewayDiscover discover = new GatewayDiscover();
            Map<InetAddress, GatewayDevice> gateways = discover.discover();

            if (gateways.isEmpty())
                return;

            activeGateway = gateways.values().iterator().next();

            String localIp = activeGateway.getLocalAddress().getHostAddress();

            boolean success = activeGateway.addPortMapping(
                    externalPort,
                    internalPort,
                    localIp,
                    protocol,
                    mappingDescription
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void destroy() {
        if (activeGateway != null) {
            try {
                boolean success = activeGateway.deletePortMapping(
                        externalPort,
                        protocol
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}