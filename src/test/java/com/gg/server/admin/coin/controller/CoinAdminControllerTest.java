package com.gg.server.admin.coin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gg.server.admin.coin.dto.CoinUpdateRequestDto;
import com.gg.server.domain.user.data.UserRepository;
import com.gg.server.global.security.jwt.utils.AuthTokenProvider;
import com.gg.server.utils.TestDataUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RequiredArgsConstructor
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CoinAdminControllerTest {
    @Autowired
    TestDataUtils testDataUtils;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    AuthTokenProvider tokenProvider;

    @Autowired
    UserRepository userRepository;

    @Test
    @DisplayName("PUT /pingpong/admin/coin")
    public void updateCoinTest() throws Exception {
        String accessToken = testDataUtils.getAdminLoginAccessToken();
        Long userId = tokenProvider.getUserIdFromAccessToken(accessToken);
        String creatorId = userRepository.getById(userId).getIntraId();
        int beforeCoin = userRepository.getById(userId).getGgCoin();
        int changeCoin = 10;
        CoinUpdateRequestDto coinUpdateRequestDto = new CoinUpdateRequestDto(creatorId, changeCoin, "관리자 코인 지급 테스트");
        mockMvc.perform(put("/pingpong/admin/coin").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(coinUpdateRequestDto)))
                .andExpect(status().isNoContent());
        Assertions.assertThat(userRepository.getById(userId).getGgCoin()).isEqualTo(beforeCoin + changeCoin);
    }
}