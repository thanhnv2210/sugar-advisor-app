package com.sugaradvisor.controller;

import com.sugaradvisor.dto.DailySummaryResponse;
import com.sugaradvisor.dto.UserCreateRequest;
import com.sugaradvisor.dto.UserResponse;
import com.sugaradvisor.dto.UserUpdateRequest;
import com.sugaradvisor.service.SummaryService;
import com.sugaradvisor.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SummaryService summaryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        return userService.createUser(request).map(UserResponse::from);
    }

    @GetMapping("/{userId}")
    public Mono<UserResponse> getUser(@PathVariable UUID userId) {
        return userService.getUser(userId).map(UserResponse::from);
    }

    @PatchMapping("/{userId}")
    public Mono<UserResponse> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UserUpdateRequest request) {
        return userService.updateUser(userId, request).map(UserResponse::from);
    }

    @GetMapping("/{userId}/summary/today")
    public Mono<DailySummaryResponse> getSummaryToday(@PathVariable UUID userId) {
        return summaryService.getUserSummaryToday(userId);
    }
}
