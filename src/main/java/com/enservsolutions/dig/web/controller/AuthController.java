package com.enservsolutions.dig.web.controller;

import com.enservsolutions.dig.dto.*;
import com.enservsolutions.dig.repository.UserRepository;
import com.enservsolutions.dig.service.AuthService;
import com.enservsolutions.dig.service.MyUserDetailsService;
import com.enservsolutions.dig.util.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@CrossOrigin
@AllArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final MyUserDetailsService userDetailsService;
    private final JwtUtil jwtTokenUtil;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @GetMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(new LoginResponse("message"));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch(BadCredentialsException ex) {
            throw new Exception("Incorrect username or password", ex);


        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
        final String loggedInUser = jwtTokenUtil.extractUsername(jwt);
        return ResponseEntity.status(HttpStatus.OK).body(new AuthenticationResponse(jwt, loggedInUser));
    }

    @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> signup(@RequestBody RegisterRequest registerRequest) throws Exception {
        try {
            authService.signup(registerRequest);
        } catch(Exception e) {
            throw new Exception("Registration not success", e);
        }
        return ResponseEntity.status(HttpStatus.OK).body(new RegisterResponse(registerRequest.getUsername()));
    }

    @GetMapping("/get-user")
    public ResponseEntity<User> getUser() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return ResponseEntity.status(HttpStatus.OK).body(principal);
    }

    @GetMapping("/get-user-info")
    public ResponseEntity<com.enservsolutions.dig.entity.User> getUserInfo() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = principal.getUsername();
        Optional<com.enservsolutions.dig.entity.User> user = userRepository.findByUsername(username);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(user.get());
    }
}

