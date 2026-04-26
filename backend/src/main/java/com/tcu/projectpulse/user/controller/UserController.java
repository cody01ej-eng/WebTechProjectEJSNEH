package com.tcu.projectpulse.user.controller;

import com.tcu.projectpulse.shared.api.Result;
import com.tcu.projectpulse.user.domain.UserRole;
import com.tcu.projectpulse.user.dto.InstructorDeactivationRequest;
import com.tcu.projectpulse.user.dto.UserAccountResponse;
import com.tcu.projectpulse.user.dto.UserProfileUpdateRequest;
import com.tcu.projectpulse.user.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasAnyRole('ADMIN','INSTRUCTOR')")
    @GetMapping
    public Result<List<UserAccountResponse>> searchUsers(@RequestParam(required = false) UserRole role,
                                                         @RequestParam(required = false) String name) {
        return Result.success("Find users success", userService.searchUsers(role, name));
    }

    @PreAuthorize("@authorizationService.canViewUser(#userId)")
    @GetMapping("/{userId}")
    public Result<UserAccountResponse> getUser(@PathVariable Long userId) {
        return Result.success("Find user success", userService.getUser(userId));
    }

    @PreAuthorize("@authorizationService.canUpdateUser(#userId)")
    @PutMapping("/{userId}")
    public Result<UserAccountResponse> updateUser(@PathVariable Long userId,
                                                  @Valid @RequestBody UserProfileUpdateRequest request) {
        return Result.success("Update user success", userService.updateProfile(userId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/deactivate")
    public Result<UserAccountResponse> deactivateInstructor(@PathVariable Long userId,
                                                            @Valid @RequestBody InstructorDeactivationRequest request) {
        return Result.success("Deactivate instructor success", userService.deactivateInstructor(userId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{userId}/reactivate")
    public Result<UserAccountResponse> reactivateInstructor(@PathVariable Long userId) {
        return Result.success("Reactivate instructor success", userService.reactivateInstructor(userId));
    }
}
