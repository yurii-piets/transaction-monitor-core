package com.tmc.property;

import com.tmc.connection.services.PropertyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.reflections.Reflections;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;

import static org.junit.Assert.assertEquals;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
public class PropertyServiceTest {

    private PropertyService propertyService;

    @Test
    public void whenNoQualifiers() {
        Reflections reflections = new Reflections("com.tmc.utils.qualifier.empty");

        propertyService = new PropertyService(reflections);
        propertyService.initQualifiers();

        Set<String> q = propertyService.getQualifiers();
        assertEquals(0, q.size());
    }

    @Test
    public void whenOneQualifier() {
        Reflections reflections = new Reflections("com.tmc.utils.qualifier.one");

        propertyService = new PropertyService(reflections);
        propertyService.initQualifiers();

        Set<String> qualifiers = propertyService.getQualifiers();
        assertEquals(1, qualifiers.size());
    }

    @Test
    public void whenFourQualifiers() {
        Reflections reflections = new Reflections("com.tmc.utils.qualifier.four");

        propertyService = new PropertyService(reflections);
        propertyService.initQualifiers();

        Set<String> qualifiers = propertyService.getQualifiers();
        assertEquals(4, qualifiers.size());
    }
}
