package com.tmc.connection.config;

import com.tmc.connection.annotation.DatabaseProperty;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class PropertyConfig {

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = getResources().toArray(new Resource[]{});
        pspc.setLocations(resources);
        pspc.setIgnoreUnresolvablePlaceholders(true);
        return pspc;
    }

    /**
     * Searches for all classed annotated with @DatabaseProperty, get path of interface
     * and creates a list of resource files where configuration of database should be
     *
     * @see DatabaseProperty
     */
    private Set<Resource> getResources() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        Set<Resource> configurationResources = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::path)
                .map(ClassPathResource::new)
                .collect(Collectors.toSet());

        return configurationResources;
    }
}
