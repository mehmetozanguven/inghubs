package com.mehmetozanguven.inghubs_digital_wallet.login;

import com.mehmetozanguven.inghubs_digital_wallet.core.commonModel.AppSecureUser;
import com.mehmetozanguven.inghubs_digital_wallet.security.config.JwtUtil;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.api.controller.LoginControllerApi;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.LoginApiResponse;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.LoginRequest;
import com.mehmetozanguven.inghubs_digital_wallet_api.contract.openapi.model.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LoginController implements LoginControllerApi {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Override
    public LoginApiResponse doLogin(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequest.getEmail(), loginRequest.getPassword()
                        )
                );
        AppSecureUser appSecureUser = (AppSecureUser) authenticate.getPrincipal();
        LoginResponse loginResponse = new LoginResponse()
                .email(appSecureUser.getUsername())
                .token(jwtUtil.generateJwtToken(appSecureUser.getUsername()));

        return new LoginApiResponse()
                .response(loginResponse)
                .isSuccess(true)
                .httpStatusCode(HttpStatus.OK.value())
                ;
    }
}
