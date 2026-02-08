package com.doogoo.doogoo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginResponseTest {

    @Test
    @DisplayName("포털 인증이 성공하면 access/refresh token과 userId, department를 반환한다")
    void 로그인_성공_테스트() {
        // given
        String portalId = "22011693";
        String portalPassword = "success";

        SejongAuthInfo authInfo = sejongAuthenticator.authenticate(portalId, portalPassword);

        String accessToken = tokenProvider.createAccessToken(authInfo.userId());
        String refreshToken = tokenProvider.createRefreshToken(authInfo.userId());

        // when
        LoginResult result = authService.login(portalId, portalPassword);

        // then
        assertThat(result.userId()).isEqualTo(22011693L);
        assertThat(result.department()).isEqualTo("컴퓨터공학과");
        assertThat(result.accessToken()).isEqualTo(accessToken);
        assertThat(result.refreshToken()).isEqualTo(refreshToken);
    }

    @Test
    @DisplayName("portalId가 누락되면 로그인은 실패한다 (400)")
    void 로그인_portalId_누락_테스트() {
        // given
        String portalId = null;
        String portalPassword = "success";

        // when & then
        assertThatThrownBy(() -> authService.login(portalId, portalPassword)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("portalId가 빈문자열이면 로그인은 실패한다 (400)")
    void 로그인_portalId_빈문자열_테스트() {
        // given
        String portalId = "";
        String portalPassword = "success";

        // when & then
        assertThatThrownBy(() -> authService.login(portalId, portalPassword)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("portalPassword가 누락되면 로그인은 실패한다 (400)")
    void 로그인_portalPassword_누락_테스트() {
        // given
        String portalId = "22011693";
        String portalPassword = null;

        // when & then
        assertThatThrownBy(() -> authService.login(portalId, portalPassword)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("portalPassword가 빈문자열이면 로그인은 실패한다 (400)")
    void 로그인_portalPassword_빈문자열_테스트() {
        // given
        String portalId = "22011693";
        String portalPassword = "";

        // when & then
        assertThatThrownBy(() -> authService.login(portalId, portalPassword)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("포털 인증이 실패하면 로그인은 실패한다 (401)")
    void 로그인_포털_인증_실패_테스트() {
        // given
        String portalId = "22011693";
        String portalPassword = "wrong";

        sejongAuthenticator.authenticate(portalId, portalPassword);

        // when & then
        assertThatThrownBy(() -> authService.login(portalId, portalPassword)).isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("세종대 포털 사이트 오류로 인해 로그인은 실패한다 (503)")
    void 포털_오류_테스트() {
        // given
        String portalId = "22011693";
        String portalPassword = "success";

        sejongAuthenticator.authenticate(portalId, portalPassword);

        // when & then
        assertThatThrownBy(() -> authService.login(portalId, portalPassword)).isInstanceOf(PortalUnavailableException.class);
    }
}
