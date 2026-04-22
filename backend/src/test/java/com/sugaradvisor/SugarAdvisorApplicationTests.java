package com.sugaradvisor;

import com.sugaradvisor.domain.Product;
import com.sugaradvisor.repository.ConsumptionRepository;
import com.sugaradvisor.repository.FamilyMemberRepository;
import com.sugaradvisor.repository.ProductRepository;
import com.sugaradvisor.repository.SugarAnalysisRepository;
import com.sugaradvisor.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SugarAdvisorApplicationTests {

    // Mock infrastructure beans so context loads without a real DB/Redis
    @MockBean UserRepository userRepository;
    @MockBean FamilyMemberRepository familyMemberRepository;
    @MockBean ConsumptionRepository consumptionRepository;
    @MockBean ProductRepository productRepository;
    @MockBean SugarAnalysisRepository sugarAnalysisRepository;
    @MockBean ReactiveRedisTemplate<String, Product> productRedisTemplate;

    @Test
    void contextLoads() {
    }
}
