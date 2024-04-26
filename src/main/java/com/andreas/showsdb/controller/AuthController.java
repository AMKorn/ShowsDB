package com.andreas.showsdb.controller;

import com.andreas.showsdb.exception.ShowsDatabaseException;
import com.andreas.showsdb.model.dto.AuthRequestDto;
import com.andreas.showsdb.model.dto.JwtResponseDto;
import com.andreas.showsdb.service.JwtService;
import com.andreas.showsdb.service.UsersService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UsersService usersService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, UsersService usersService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.usersService = usersService;
    }

    @PostMapping("/api/login")
    public JwtResponseDto authenticateAndGetToken(@RequestBody AuthRequestDto authRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequestDTO.getUsername(), authRequestDTO.getPassword()));
        if (authentication.isAuthenticated()) {
            String accessToken = jwtService.GenerateToken(authRequestDTO.getUsername());
            System.out.println(accessToken);
            return JwtResponseDto.builder()
                    .accessToken(accessToken)
                    .build();
        } else {
            throw new ShowsDatabaseException("Bad credentials", HttpStatus.BAD_REQUEST);
        }
    }

}
