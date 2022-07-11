package com.alexeykovzel.insidr;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessCIKDataTest {
    private CIKDataService service;

    @BeforeEach
    public void setUp() {
        service = new CIKDataService();
    }

    @Test
    public void saveData() {
        service.saveIndexesToFile();
    }

    @Test
    public void collectData() {
        Map<String, List<String>> indexes = service.getIndexes(4, 7);
        assertEquals(3, indexes.size());
        assertTrue(indexes.containsKey("0001427189"));
        assertTrue(indexes.containsKey("0001655250"));
        assertTrue(indexes.containsKey("0001447162"));
    }
}
