package io.github.lucasssoh.boussole.server.web;

import java.io.IOException;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the Angular interface. A path that is not a file of the interface is one of its pages,
 * such as {@code /subjects/oc/sessions/s34}: the interface's {@code index.html} answers it.
 */
@Configuration(proxyBeanMethods = false)
class InterfaceConfiguration implements WebMvcConfigurer {

    private static final String LOCATION = "classpath:/web/browser/";
    private static final Resource INDEX = new ClassPathResource("web/browser/index.html");

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations(LOCATION)
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String path, Resource location) throws IOException {
                        Resource found = super.getResource(path, location);
                        return found != null || path.startsWith("api/") ? found : INDEX;
                    }
                });
    }
}
