package com.tmc.services;

import com.tmc.annotation.DatabaseProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class DatabasePropertyService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    @Bean
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(){
        PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer = new PropertySourcesPlaceholderConfigurer();
        Resource[] resources = getResources().toArray(new Resource[]{});
        propertySourcesPlaceholderConfigurer.setLocations(resources);
        propertySourcesPlaceholderConfigurer.setIgnoreUnresolvablePlaceholders(true);
        return propertySourcesPlaceholderConfigurer;
    }

    /**
     * Searches for all classed annotated with @DatabaseProperty, get value of interface
     * and creates a list of files where configuration of database should be
     *
     * @see DatabaseProperty
     */
    public Set<File> getFiles() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        Set<File> configurationFiles = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::value)
                .map(ClassPathResource::new)
                .map(classPathResource -> {
                    File file = null;
                    try {
                        file = classPathResource.getFile();
                    } catch (IOException e) {
                        logger.error("Unexpected error while searching for dbConfig file(s): ", e.getMessage());
                    }
                    return file;
                })
                .collect(Collectors.toSet());

        return configurationFiles;
    }

    /**
     * Searches for all classed annotated with @DatabaseProperty, get value of interface
     * and creates a list of resource files where configuration of database should be
     *
     * @see DatabaseProperty
     */
    private Set<Resource> getResources() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        Set<Resource> configurationFiles = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::value)
                .map(ClassPathResource::new)
                .collect(Collectors.toSet());

        return configurationFiles;
    }


    /**
     * Searches for all classed annotated with @DatabaseProperty, get value of interface
     * and creates a list of name where configuration of database should be
     *
     * @see DatabaseProperty
     */
    public Set<String> getNames(){
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        Set<String> configurationNames = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::value)
                .collect(Collectors.toSet());

        return configurationNames;
    }

}
