package io.github.lucasssoh.boussole.server.web;

import io.github.lucasssoh.boussole.server.ServerSettings;
import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.SSLHostConfig;
import org.apache.tomcat.util.net.SSLHostConfigCertificate;
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * When the configuration lets other devices in, a second listener, over HTTPS, on every address
 * of the computer. The first one stays on 127.0.0.1, for this computer's browser, without a
 * certificate to accept. {@link AccessFilter} tells the two apart by their port.
 */
@Component
class NetworkConnector implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final ServerSettings settings;

    NetworkConnector(ServerSettings settings) {
        this.settings = settings;
    }

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        settings.network().ifPresent(network -> {
            Connector connector = new Connector(Http11NioProtocol.class.getName());
            connector.setPort(network.port());
            connector.setScheme("https");
            connector.setSecure(true);
            Http11NioProtocol protocol = (Http11NioProtocol) connector.getProtocolHandler();
            protocol.setSSLEnabled(true);
            SSLHostConfig ssl = new SSLHostConfig();
            SSLHostConfigCertificate certificate = new SSLHostConfigCertificate(ssl, SSLHostConfigCertificate.Type.UNDEFINED);
            certificate.setCertificateKeystoreFile(network.keystore().toAbsolutePath().toString());
            certificate.setCertificateKeystorePassword(network.password());
            certificate.setCertificateKeystoreType("PKCS12");
            ssl.addCertificate(certificate);
            connector.addSslHostConfig(ssl);
            factory.addAdditionalConnectors(connector);
        });
    }
}
