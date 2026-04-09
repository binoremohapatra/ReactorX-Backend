package com.reactorx.controller;

import com.reactorx.dto.StaticPageDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/static")
public class StaticPageController {

    // Inject a service or directly return content for simplicity
    @GetMapping("/{pageSlug}")
    public ResponseEntity<StaticPageDTO> getStaticPage(@PathVariable String pageSlug) {
        // In a real app, fetch content based on slug from DB or file
        StaticPageDTO dto = new StaticPageDTO();
        switch (pageSlug) {
            case "faq":
                dto.setTitle("Frequently Asked Questions");
                dto.setContent("Content for FAQ page...");
                break;
            case "privacy-policy":
                 dto.setTitle("Privacy Policy");
                 dto.setContent("Content for Privacy Policy page...");
                 break;
             // Add cases for other static pages
            default:
                return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(dto);
    }
}

