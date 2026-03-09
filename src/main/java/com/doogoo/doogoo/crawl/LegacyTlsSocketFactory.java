package com.doogoo.doogoo.crawl;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * TLSv1.0/1.1 등 구형 cipher를 사용하는 서버(세종대 포털)와의 연결을 위해
 * 레거시 TLS 프로토콜을 활성화하는 SSLSocketFactory.
 */
public class LegacyTlsSocketFactory extends SSLSocketFactory {

    private static final String[] ENABLED_PROTOCOLS = {"TLSv1", "TLSv1.1", "TLSv1.2", "TLSv1.3"};

    private final SSLSocketFactory delegate;

    public LegacyTlsSocketFactory(SSLSocketFactory delegate) {
        this.delegate = delegate;
    }

    private Socket enableLegacyTls(Socket socket) {
        if (socket instanceof SSLSocket ssl) {
            ssl.setEnabledProtocols(ENABLED_PROTOCOLS);
        }
        return socket;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return enableLegacyTls(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return enableLegacyTls(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return enableLegacyTls(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableLegacyTls(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableLegacyTls(delegate.createSocket(address, port, localAddress, localPort));
    }
}
