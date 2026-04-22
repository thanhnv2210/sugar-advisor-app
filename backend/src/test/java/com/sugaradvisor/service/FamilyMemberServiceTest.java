package com.sugaradvisor.service;

import com.sugaradvisor.domain.FamilyMember;
import com.sugaradvisor.domain.User;
import com.sugaradvisor.dto.FamilyMemberRequest;
import com.sugaradvisor.repository.FamilyMemberRepository;
import com.sugaradvisor.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FamilyMemberServiceTest {

    @Mock
    FamilyMemberRepository familyMemberRepository;

    @Mock
    UserRepository userRepository;

    @InjectMocks
    FamilyMemberService service;

    private final UUID USER_ID = UUID.randomUUID();
    private final UUID MEMBER_ID = UUID.randomUUID();

    @Test
    void create_savesNewMemberScopedToUser() {
        User user = User.builder().id(USER_ID).name("Parent").build();
        when(userRepository.findById(USER_ID)).thenReturn(Mono.just(user));

        FamilyMember saved = FamilyMember.builder().id(MEMBER_ID).userId(USER_ID)
                .name("Child").age(5).relation("child").dailySugarLimit(BigDecimal.valueOf(25)).build();
        when(familyMemberRepository.save(any())).thenReturn(Mono.just(saved));

        FamilyMemberRequest request = new FamilyMemberRequest("Child", 5, "child", BigDecimal.valueOf(25));

        StepVerifier.create(service.create(USER_ID, request))
                .assertNext(m -> {
                    assertThat(m.getName()).isEqualTo("Child");
                    assertThat(m.getUserId()).isEqualTo(USER_ID);
                })
                .verifyComplete();
    }

    @Test
    void create_userNotFound_throwsError() {
        when(userRepository.findById(USER_ID)).thenReturn(Mono.empty());

        FamilyMemberRequest request = new FamilyMemberRequest("Child", 5, "child", BigDecimal.valueOf(25));

        StepVerifier.create(service.create(USER_ID, request))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("User not found"))
                .verify();
    }

    @Test
    void listByUser_returnsOnlyUserMembers() {
        FamilyMember m1 = FamilyMember.builder().id(UUID.randomUUID()).userId(USER_ID).name("Child1").build();
        FamilyMember m2 = FamilyMember.builder().id(UUID.randomUUID()).userId(USER_ID).name("Child2").build();
        when(familyMemberRepository.findByUserId(USER_ID)).thenReturn(Flux.just(m1, m2));

        StepVerifier.create(service.listByUser(USER_ID))
                .assertNext(m -> assertThat(m.getName()).isEqualTo("Child1"))
                .assertNext(m -> assertThat(m.getName()).isEqualTo("Child2"))
                .verifyComplete();
    }

    @Test
    void getOne_memberBelongsToCorrectUser_returnsIt() {
        FamilyMember member = FamilyMember.builder().id(MEMBER_ID).userId(USER_ID).name("Child").build();
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.just(member));

        StepVerifier.create(service.getOne(USER_ID, MEMBER_ID))
                .assertNext(m -> assertThat(m.getId()).isEqualTo(MEMBER_ID))
                .verifyComplete();
    }

    @Test
    void getOne_memberBelongsToDifferentUser_throwsMemberNotFound() {
        UUID otherUserId = UUID.randomUUID();
        FamilyMember member = FamilyMember.builder().id(MEMBER_ID).userId(otherUserId).name("Child").build();
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.just(member));

        StepVerifier.create(service.getOne(USER_ID, MEMBER_ID))
                .expectError(FamilyMemberService.MemberNotFoundException.class)
                .verify();
    }

    @Test
    void getOne_memberNotInRepo_throwsMemberNotFound() {
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(service.getOne(USER_ID, MEMBER_ID))
                .expectError(FamilyMemberService.MemberNotFoundException.class)
                .verify();
    }

    @Test
    void update_onlyUpdatesNonNullFields() {
        FamilyMember existing = FamilyMember.builder()
                .id(MEMBER_ID).userId(USER_ID).name("OldName").age(5).relation("child").build();
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.just(existing));

        ArgumentCaptor<FamilyMember> captor = ArgumentCaptor.forClass(FamilyMember.class);
        when(familyMemberRepository.save(captor.capture())).thenReturn(Mono.just(existing));

        FamilyMemberRequest request = new FamilyMemberRequest("NewName", null, null, null);

        StepVerifier.create(service.update(USER_ID, MEMBER_ID, request))
                .expectNextCount(1)
                .verifyComplete();

        assertThat(captor.getValue().getName()).isEqualTo("NewName");
        assertThat(captor.getValue().getAge()).isEqualTo(5);
        assertThat(captor.getValue().getRelation()).isEqualTo("child");
    }

    @Test
    void delete_memberNotFound_throwsError() {
        when(familyMemberRepository.findById(MEMBER_ID)).thenReturn(Mono.empty());

        StepVerifier.create(service.delete(USER_ID, MEMBER_ID))
                .expectError(FamilyMemberService.MemberNotFoundException.class)
                .verify();
    }
}
