package com.vadim.ok.portmapper.config;

public class ServiceDescriptor {
    private String serviceId; // like "web", "jabber" etc.
    private boolean isHttpBasedProtocol; // indicate if we need to map HTTP headers (like "Host:" ect)
    private int localPort;
    private String remoteHost;
    private int remotePort;

    public ServiceDescriptor(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceId() {
        return serviceId;
    }

    void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public boolean isHttpBasedProtocol() {
        return isHttpBasedProtocol;
    }

    void setHttpBasedProtocol(boolean isHttp) {
        this.isHttpBasedProtocol = isHttp;
    }

    public int getLocalPort() {
        return localPort;
    }

    void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }
}
