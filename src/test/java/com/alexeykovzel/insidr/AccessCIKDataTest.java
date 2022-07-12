package com.alexeykovzel.insidr;

import com.alexeykovzel.insidr.cik.CIKDataService;
import com.alexeykovzel.insidr.cik.CentralIndexKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AccessCIKDataTest {
    private final static String FILEPATH = "src/main/resources/cik-data.txt";
    private CIKDataService cikService;

    @BeforeEach
    public void setUp() {
        cikService = new CIKDataService();
    }

    @Test
    public void saveData() throws Exception {
        cikService.saveIndexesToFile(FILEPATH);
        try (BufferedReader in = new BufferedReader(new FileReader(FILEPATH))) {
            assertTrue(in.lines().findAny().isPresent());
        }
    }

    @Test
    public void collectData() {
        long t1 = System.currentTimeMillis();
        List<CentralIndexKey> indexes = cikService.getIndexes(0, 50000);
        long t2 = System.currentTimeMillis();
        System.out.printf("Passed: %d milliseconds\n", t2 - t1);

//        assertEquals(696, indexes.size());
//        assertEquals("0001427189", indexes.get(0).getKey());
//        assertEquals("0001655250", indexes.get(1).getKey());
//        assertEquals("0001447162", indexes.get(2).getKey());
    }
}
