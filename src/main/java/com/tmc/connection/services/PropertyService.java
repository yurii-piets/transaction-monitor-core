package com.tmc.connection.services;

import com.tmc.connection.annotation.DatabaseProperty;
import lombok.Getter;
import org.reflections.Reflections;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PropertyService {

    @Getter
    private Set<String> qualifiers;

    @PostConstruct
    public void initQualifiers() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        this.qualifiers = configs.stream()
                .map(c -> c.getAnnotation(DatabaseProperty.class))
                .map(DatabaseProperty::qualifiers)
                .flatMap(Arrays::stream)
                .collect(Collectors.toSet());
    }
}
