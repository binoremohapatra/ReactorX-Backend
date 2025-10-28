package com.reactorx.dto;

import lombok.Data;

@Data
public class StaticPageDTO {
    private String title;
    private String content; // Could be HTML or Markdown
}

