package com.tmc.connection.services;

import com.tmc.connection.annotation.DatabaseProperty;
import lombok.Getter;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Search and contains qualifiers that user had specified
 *
 * @see DatabaseProperty
 */
@Service
public class PropertyService {

    /**
     * Set of qualifiers specified by user
     */
    @Getter
    private Set<String> qualifiers;

    private final Reflections reflections;

    @Autowired
    public PropertyService(Reflections reflections) {
        this.reflections = reflections;
    }

    /**
     * Using reflection searches for usage of @DatabaseConfiguration
     * and retries array of qualifiers specified by user
     */
    @PostConstruct
    public void initQualifiers() {
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        this.qualifiers = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::qualifiers)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }
}
