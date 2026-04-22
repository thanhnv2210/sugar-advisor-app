package com.sugaradvisor.service;

import com.sugaradvisor.domain.SugarAnalysis;
import com.sugaradvisor.domain.User;
import com.sugaradvisor.dto.SugarAnalysisRequest;
import com.sugaradvisor.repository.SugarAnalysisRepository;
import com.sugaradvisor.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SugarAnalysisServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    ConsumptionService consumptionService;

    @Mock
    SugarAnalysisRepository sugarAnalysisRepository;

    @InjectMocks
    SugarAnalysisService service;

    private final UUID USER_ID = UUID.randomUUID();

    @Test
    void analyze_sugarBelow5g_returnsLowLevel() {
        mockUserWithLimit(50.0);
        mockTodayTotal(10.0);
        mockSave("LOW", false, BigDecimal.valueOf(36.0));

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(4.0), null);

        StepVerifier.create(service.analyze(request))
                .assertNext(r -> {
                    assertThat(r.sugarLevel()).isEqualTo("LOW");
                    assertThat(r.isExceedLimit()).isFalse();
                })
                .verifyComplete();
    }

    @Test
    void analyze_sugarBetween5And15g_returnsMediumLevel() {
        mockUserWithLimit(50.0);
        mockTodayTotal(0.0);
        mockSave("MEDIUM", false, BigDecimal.valueOf(40.0));

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(10.0), null);

        StepVerifier.create(service.analyze(request))
                .assertNext(r -> assertThat(r.sugarLevel()).isEqualTo("MEDIUM"))
                .verifyComplete();
    }

    @Test
    void analyze_sugarAbove15g_returnsHighLevel() {
        mockUserWithLimit(50.0);
        mockTodayTotal(0.0);
        mockSave("HIGH", false, BigDecimal.valueOf(30.0));

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(20.0), null);

        StepVerifier.create(service.analyze(request))
                .assertNext(r -> assertThat(r.sugarLevel()).isEqualTo("HIGH"))
                .verifyComplete();
    }

    @Test
    void analyze_sugarExceedsRemainingBudget_setsExceedLimitTrue() {
        mockUserWithLimit(50.0);
        mockTodayTotal(45.0); // only 5g left
        mockSave("HIGH", true, BigDecimal.valueOf(-15.0));

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(20.0), null);

        StepVerifier.create(service.analyze(request))
                .assertNext(r -> assertThat(r.isExceedLimit()).isTrue())
                .verifyComplete();
    }

    @Test
    void analyze_sugarWithinBudget_setsExceedLimitFalse() {
        mockUserWithLimit(50.0);
        mockTodayTotal(10.0); // 40g left
        mockSave("LOW", false, BigDecimal.valueOf(36.0));

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(4.0), null);

        StepVerifier.create(service.analyze(request))
                .assertNext(r -> assertThat(r.isExceedLimit()).isFalse())
                .verifyComplete();
    }

    @Test
    void analyze_userHasNullLimit_usesDefault50g() {
        User user = User.builder().id(USER_ID).name("Test").dailySugarLimit(null).build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(user));
        mockTodayTotal(0.0);
        mockSave("LOW", false, BigDecimal.valueOf(46.0));

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(4.0), null);

        // With default 50g, 4g is well within limit
        StepVerifier.create(service.analyze(request))
                .assertNext(r -> assertThat(r.isExceedLimit()).isFalse())
                .verifyComplete();
    }

    @Test
    void analyze_userNotFound_throwsError() {
        when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());

        SugarAnalysisRequest request = new SugarAnalysisRequest(USER_ID, null, BigDecimal.valueOf(10.0), null);

        StepVerifier.create(service.analyze(request))
                .expectErrorMessage("User not found: " + USER_ID)
                .verify();
    }

    // --- Helpers ---

    private void mockUserWithLimit(double limit) {
        User user = User.builder().id(USER_ID).name("Test")
                .dailySugarLimit(BigDecimal.valueOf(limit)).build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(user));
    }

    private void mockTodayTotal(double total) {
        when(consumptionService.getTodayTotal(USER_ID)).thenReturn(Mono.just(total));
    }

    private void mockSave(String level, boolean exceed, BigDecimal remaining) {
        SugarAnalysis saved = SugarAnalysis.builder()
                .id(UUID.randomUUID()).userId(USER_ID)
                .sugarAmount(BigDecimal.TEN)
                .sugarLevel(level)
                .isExceedLimit(exceed)
                .remainingSugar(remaining)
                .recommendation("test recommendation")
                .createdAt(LocalDateTime.now())
                .build();
        when(sugarAnalysisRepository.save(any())).thenReturn(Mono.just(saved));
    }
}
