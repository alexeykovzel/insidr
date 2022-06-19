package com.alexeykovzel.insidr.insiders;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InsidersController {

    @GetMapping("/")
    public String insiders() {
        return "Information about insiders";
    }

}
