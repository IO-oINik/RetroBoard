package ru.edu.retro.apiservice.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.edu.retro.apiservice.models.dto.responses.SVGTemplateResponse;
import ru.edu.retro.apiservice.services.SvgTemplateService;

import java.util.List;

@RestController
@RequestMapping("/svg-template")
@RequiredArgsConstructor
public class SvgController {
    private final SvgTemplateService svgTemplateService;

    @GetMapping("/all")
    public ResponseEntity<List<SVGTemplateResponse>> getList() {
        return ResponseEntity.ok(svgTemplateService.getAll());
    }
}
