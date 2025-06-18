package ru.edu.retro.apiservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.edu.retro.apiservice.models.dto.requests.ComponentEditRequest;
import ru.edu.retro.apiservice.models.dto.responses.ComponentResponse;
import ru.edu.retro.apiservice.services.ComponentService;

import java.util.UUID;

@RestController
@RequestMapping("/components")
@RequiredArgsConstructor
@Tag(name = "Component")
public class ComponentController {
    private final ComponentService componentService;

    @Operation(summary = "Изменить компонент по ID")
    @PatchMapping("/{id}")
    public ResponseEntity<ComponentResponse> editById(@PathVariable("id") UUID id, @Valid @RequestBody ComponentEditRequest request) {
        return ResponseEntity.ok(componentService.editById(id, request));
    }

    @Operation(summary = "Удалить компонент по ID (только для владельца)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") UUID id) {
        componentService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Проголосовать за компонент")
    @PostMapping("/{id}/vote")
    public ResponseEntity<Void> vote(@PathVariable("id") UUID id) {
        componentService.addVote(id);
        return ResponseEntity.status(201).build();
    }

    @Operation(summary = "Удалить голос")
    @DeleteMapping("{id}/vote")
    public ResponseEntity<Void> deleteVote(@PathVariable("id") UUID id) {
        componentService.removeVote(id);
        return ResponseEntity.noContent().build();
    }
}
