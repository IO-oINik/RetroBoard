package ru.edu.retro.apiservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.edu.retro.apiservice.models.dto.requests.UserEditRequest;
import ru.edu.retro.apiservice.models.dto.responses.UserResponse;
import ru.edu.retro.apiservice.services.UserService;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Информация о себе (Бесполезная вещь)")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(userService.findMe());
    }

    @Operation(summary = "Изменить информацию о себе (Еще одна бесполезная вещь)")
    @PatchMapping("/me")
    public ResponseEntity<UserResponse> editMe(@Valid @RequestBody UserEditRequest request) {
        return ResponseEntity.ok(userService.editMe(request));
    }
}
