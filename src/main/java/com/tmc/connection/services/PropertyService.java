package com.tmc.connection.services;

import com.tmc.connection.annotation.DatabaseProperty;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Search and contains qualifiers that user had specified
 *
 * @see DatabaseProperty
 */
public class PropertyService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Set of qualifiers specified by user
     */
    @Getter
    private Set<String> qualifiers;

    private Set<Properties> properties;

    public PropertyService() {
        initQualifiers();
        initProperties();
    }

    /**
     * Using reflection searches for usage of @DatabaseConfiguration
     * and retries array of qualifiers specified by user
     */
    private void initQualifiers() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        this.qualifiers = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::qualifiers)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }

    /**
     * Searches for all classed annotated with @DatabaseProperty, get path of interface
     * and creates a list of resource files where configuration of database should be
     *
     * @see DatabaseProperty
     */
    private void initProperties() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        this.properties = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::path)
                .map(path -> {
                    Properties properties = new Properties();

                    try {
                        properties.load(getClass().getClassLoader().getResourceAsStream(path));
                    } catch (IOException e) {
                        logger.error("Unexpected: ", e);
                    }

                    return properties;
                })
                .collect(Collectors.toSet());
    }

    public String getRequiredProperty(String key) {
        String property = getPropertyByName(key);

        if (property == null) {
            throw new IllegalStateException("Could not find property with the name: " + key);
        }

        return property;
    }

    private String getPropertyByName(String key) {
        for (Properties property : properties) {
            if (property.containsKey(key)) {
                return property.getProperty(key);
            }
        }

        return null;
    }
}
