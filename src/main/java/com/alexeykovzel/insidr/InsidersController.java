package com.alexeykovzel.insidr;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InsidersController {

    @GetMapping("/cik")
    public String insiders() {
        return "Information about insiders";
    }
}
