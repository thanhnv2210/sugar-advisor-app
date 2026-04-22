package com.sugaradvisor.service;

import com.sugaradvisor.domain.User;
import com.sugaradvisor.dto.UserCreateRequest;
import com.sugaradvisor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    // WHO upper daily sugar limit for adults (grams)
    private static final BigDecimal DEFAULT_DAILY_SUGAR_LIMIT = BigDecimal.valueOf(50.0);

    private final UserRepository userRepository;

    public Mono<User> createUser(UserCreateRequest request) {
        User user = User.builder()
                .name(request.name())
                .age(request.age())
                .weight(request.weight())
                .dailySugarLimit(DEFAULT_DAILY_SUGAR_LIMIT)
                .build();
        return userRepository.save(user);
    }

    public Mono<User> getUser(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("User not found: " + userId)));
    }
}
