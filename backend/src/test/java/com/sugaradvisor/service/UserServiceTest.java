package com.sugaradvisor.service;

import com.sugaradvisor.domain.User;
import com.sugaradvisor.dto.UserCreateRequest;
import com.sugaradvisor.dto.UserUpdateRequest;
import com.sugaradvisor.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @InjectMocks
    UserService service;

    private final UUID USER_ID = UUID.randomUUID();

    @Test
    void createUser_setsDefaultDailySugarLimitTo50g() {
        UserCreateRequest request = new UserCreateRequest("Alice", 30, BigDecimal.valueOf(60.0));
        User saved = User.builder().id(USER_ID).name("Alice").dailySugarLimit(BigDecimal.valueOf(50.0)).build();
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createUser(request))
                .assertNext(u -> assertThat(u.getDailySugarLimit()).isEqualByComparingTo("50.0"))
                .verifyComplete();
    }

    @Test
    void createUser_persistsNameFromRequest() {
        UserCreateRequest request = new UserCreateRequest("Bob", null, null);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        User saved = User.builder().id(USER_ID).name("Bob").dailySugarLimit(BigDecimal.valueOf(50.0)).build();
        when(userRepository.save(captor.capture())).thenReturn(Mono.just(saved));

        StepVerifier.create(service.createUser(request))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(captor.getValue().getName()).isEqualTo("Bob");
        assertThat(captor.getValue().getDailySugarLimit()).isEqualByComparingTo("50.0");
    }

    @Test
    void getUser_notFound_throwsRuntimeException() {
        when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(service.getUser(USER_ID))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("User not found"))
                .verify();
    }

    @Test
    void updateUser_onlyUpdatesNonNullFields() {
        User existing = User.builder().id(USER_ID).name("OldName").age(25)
                .dailySugarLimit(BigDecimal.valueOf(50)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(existing));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenReturn(Mono.just(existing));

        // Only update name; age and dailySugarLimit are null → should not change
        UserUpdateRequest request = new UserUpdateRequest("NewName", null, null, null);

        StepVerifier.create(service.updateUser(USER_ID, request))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(captor.getValue().getName()).isEqualTo("NewName");
        assertThat(captor.getValue().getAge()).isEqualTo(25);
    }

    @Test
    void updateUser_updatesCustomDailySugarLimit() {
        User existing = User.builder().id(USER_ID).name("Alice")
                .dailySugarLimit(BigDecimal.valueOf(50)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(existing));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(captor.capture())).thenReturn(Mono.just(existing));

        UserUpdateRequest request = new UserUpdateRequest(null, null, null, BigDecimal.valueOf(35));

        StepVerifier.create(service.updateUser(USER_ID, request))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(captor.getValue().getDailySugarLimit()).isEqualByComparingTo("35");
    }

    @Test
    void updateUser_notFound_throwsRuntimeException() {
        when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(service.updateUser(USER_ID, new UserUpdateRequest("X", null, null, null)))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("User not found"))
                .verify();
    }
}
