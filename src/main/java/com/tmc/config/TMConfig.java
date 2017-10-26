package com.tmc.config;

import com.tmc.annotation.DatabaseProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TMConfig defines a Spring Context to make possible usage of Java Beans and Spring Components injections
 * and using all Spring framework features
 */
@SpringBootApplication
@Getter
@Setter
public class TMConfig {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private Set<File> configurationFiles;

    public void init(String[] args) {
        SpringApplication.run(TMConfig.class, args);
        initConfigPropertyFiles();
    }

    /**
     * Searches for all classed annotated with @DbSource, get value of interface
     * and creates a list of files where configuration of database should be
     *
     * @see DatabaseProperty
     */
    private void initConfigPropertyFiles() {
        Reflections reflections = new Reflections();
        Set<Class<?>> configs = reflections.getTypesAnnotatedWith(DatabaseProperty.class);

        this.configurationFiles = configs.stream()
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
    }
}
