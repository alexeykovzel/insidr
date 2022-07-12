package com.alexeykovzel.insidr.cik;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CIKController {
    private final CIKDataService cikService;

    @Autowired
    public CIKController(CIKDataService cikService) {
        this.cikService = cikService;
    }

    @GetMapping("/cik")
    public List<CentralIndexKey> getIndexes(@RequestParam(name = "from", defaultValue = "0") int from,
                                            @RequestParam(name = "to") int to) {
        return cikService.getIndexes(from, to);
    }
}
