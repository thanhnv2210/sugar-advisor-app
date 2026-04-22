package com.sugaradvisor.service;

import com.sugaradvisor.domain.FamilyMember;
import com.sugaradvisor.domain.User;
import com.sugaradvisor.repository.FamilyMemberRepository;
import com.sugaradvisor.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    FamilyMemberRepository familyMemberRepository;

    @Mock
    ConsumptionService consumptionService;

    @InjectMocks
    SummaryService service;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID MEMBER_ID = UUID.randomUUID();

    // --- User summary tests ---

    @Test
    void getUserSummary_belowHalfLimit_returnsLowStatus() {
        mockUser(50.0);
        mockUserConsumption(10.0, 2); // ratio = 0.2 → LOW

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .assertNext(r -> {
                    assertThat(r.sugarStatus()).isEqualTo("LOW");
                    assertThat(r.totalSugar()).isEqualTo(10.0);
                    assertThat(r.dailyLimit()).isEqualTo(50.0);
                    assertThat(r.remaining()).isEqualTo(40.0);
                    assertThat(r.itemCount()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void getUserSummary_between50And80Percent_returnsMediumStatus() {
        mockUser(50.0);
        mockUserConsumption(30.0, 3); // ratio = 0.6 → MEDIUM

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .assertNext(r -> assertThat(r.sugarStatus()).isEqualTo("MEDIUM"))
                .verifyComplete();
    }

    @Test
    void getUserSummary_above80Percent_returnsHighStatus() {
        mockUser(50.0);
        mockUserConsumption(45.0, 5); // ratio = 0.9 → HIGH

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .assertNext(r -> assertThat(r.sugarStatus()).isEqualTo("HIGH"))
                .verifyComplete();
    }

    @Test
    void getUserSummary_exactly80Percent_returnsHighStatus() {
        mockUser(50.0);
        mockUserConsumption(40.0, 4); // ratio = 0.8 → HIGH (not < 0.8)

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .assertNext(r -> assertThat(r.sugarStatus()).isEqualTo("HIGH"))
                .verifyComplete();
    }

    @Test
    void getUserSummary_userHasNullLimit_usesDefault50g() {
        User user = User.builder().id(USER_ID).name("Test").dailySugarLimit(null).build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(user));
        mockUserConsumption(10.0, 1);

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .assertNext(r -> assertThat(r.dailyLimit()).isEqualTo(50.0))
                .verifyComplete();
    }

    @Test
    void getUserSummary_remainingNeverBelowZero_whenOverLimit() {
        mockUser(50.0);
        mockUserConsumption(60.0, 6); // exceeded

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .assertNext(r -> assertThat(r.remaining()).isEqualTo(0.0))
                .verifyComplete();
    }

    @Test
    void getUserSummary_userNotFound_throwsError() {
        when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(service.getUserSummaryToday(USER_ID))
                .expectErrorMessage("User not found: " + USER_ID)
                .verify();
    }

    // --- Family member summary tests ---

    @Test
    void getFamilyMemberSummary_usesCustomMemberLimit() {
        FamilyMember member = FamilyMember.builder()
                .id(MEMBER_ID).userId(USER_ID).name("Child")
                .dailySugarLimit(BigDecimal.valueOf(25.0)).build();
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.just(member));
        mockMemberConsumption(5.0, 1);

        StepVerifier.create(service.getFamilyMemberSummaryToday(USER_ID, MEMBER_ID))
                .assertNext(r -> {
                    assertThat(r.dailyLimit()).isEqualTo(25.0);
                    assertThat(r.remaining()).isEqualTo(20.0);
                    assertThat(r.totalSugar()).isEqualTo(5.0);
                })
                .verifyComplete();
    }

    @Test
    void getFamilyMemberSummary_memberHasNullLimit_usesDefault50g() {
        FamilyMember member = FamilyMember.builder()
                .id(MEMBER_ID).userId(USER_ID).name("Child").dailySugarLimit(null).build();
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.just(member));
        mockMemberConsumption(5.0, 1);

        StepVerifier.create(service.getFamilyMemberSummaryToday(USER_ID, MEMBER_ID))
                .assertNext(r -> assertThat(r.dailyLimit()).isEqualTo(50.0))
                .verifyComplete();
    }

    @Test
    void getFamilyMemberSummary_wrongUser_throwsMemberNotFound() {
        UUID otherUserId = UUID.randomUUID();
        FamilyMember member = FamilyMember.builder()
                .id(MEMBER_ID).userId(otherUserId).name("Child").build();
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.just(member));

        StepVerifier.create(service.getFamilyMemberSummaryToday(USER_ID, MEMBER_ID))
                .expectError(FamilyMemberService.MemberNotFoundException.class)
                .verify();
    }

    // --- Helpers ---

    private void mockUser(double limit) {
        User user = User.builder().id(USER_ID).name("Test")
                .dailySugarLimit(BigDecimal.valueOf(limit)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(user));
    }

    private void mockUserConsumption(double total, int count) {
        when(consumptionService.getTodayTotal(USER_ID)).thenReturn(Mono.just(total));
        when(consumptionService.getTodayCount(USER_ID)).thenReturn(Mono.just(count));
    }

    private void mockMemberConsumption(double total, int count) {
        when(consumptionService.getTodayTotalByFamilyMember(MEMBER_ID)).thenReturn(Mono.just(total));
        when(consumptionService.getTodayCountByFamilyMember(MEMBER_ID)).thenReturn(Mono.just(count));
    }
}
