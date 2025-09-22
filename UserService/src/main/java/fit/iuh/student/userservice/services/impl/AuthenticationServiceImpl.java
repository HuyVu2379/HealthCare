package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.AuthenticationRequest;
import fit.iuh.student.userservice.dtos.requests.RegisterRequest;
import fit.iuh.student.userservice.dtos.requests.ResetPasswordRequest;
import fit.iuh.student.userservice.dtos.responses.*;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.entities.Patient;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import fit.iuh.student.userservice.exceptions.errors.DuplicateUserException;
import fit.iuh.student.userservice.exceptions.errors.UnauthorizedException;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.mappers.UserMapper;
import fit.iuh.student.userservice.publishers.payload.UserEventPayload;
import fit.iuh.student.userservice.repositories.DoctorRepository;
import fit.iuh.student.userservice.repositories.UserRepository;
import fit.iuh.student.userservice.services.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of AuthenticationService
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final DoctorRepository doctorRepository;
    private final DoctorService doctorService;
    private final UserMapper userMapper;
    private final PatientService patientService;
    private final EmailService emailService;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationServiceImpl.class);
    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${jwt.prefix}")
    private String tokenPrefix;

    @Override
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        Patient user = new Patient();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.PATIENT);
        user.setStatus(Status.INACTIVE);
        // Send OTP email
        try {
            emailService.sendOTPEmail(new UserEventPayload(request.getEmail(),"Xác thực đăng ký tài khoản"));
        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", request.getEmail(), e);
            throw new RuntimeException("Failed to send OTP email");
        }
        // Save user
        userRepository.save(user);

        // Generate JWT token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("email", user.getEmail());


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(extraClaims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Return authentication response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenPrefix)
                .expiresIn(jwtExpiration / 1000)
                .build();
    }

    @Override
    public boolean verifyAccount(String email, String otp) {
        try{
            boolean isValid = emailService.validateOTP(email,otp);
            if (!isValid) {
                throw new UnauthorizedException("Invalid OTP");
            }
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                throw new UserNotFoundException("User not found");
            }
            User user = userOpt.get();
            if (user.getStatus() == Status.ACTIVE) {
                return true; // Tài khoản đã được kích hoạt
            }
            user.setStatus(Status.ACTIVE);
            userRepository.save(user);
            return true;
        }catch (Exception e){
            log.error("Error verifying account for email {}: {}", email, e.getMessage());
            throw e;
        }
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Get user
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Generate JWT token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("email", user.getEmail());
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(extraClaims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Return authentication response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenPrefix)
                .expiresIn(jwtExpiration / 1000)
                .build();
    }

    @Override
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        // Kiểm tra header Authorization
        if (authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
            return null;
        }
        refreshToken = authHeader.substring(tokenPrefix.length() + 1);
        try {
            userEmail = jwtService.extractUsername(refreshToken);
            if (userEmail != null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);
                if (jwtService.isTokenValid(refreshToken, userDetails)) {
                    User user = userRepository.findByEmail(userEmail)
                            .orElseThrow(() -> new UnauthorizedException("User not found"));

                    Map<String, Object> extraClaims = new HashMap<>();
                    extraClaims.put("role", user.getRole().name());
                    extraClaims.put("userId", user.getUserId());
                    extraClaims.put("email", user.getEmail());
                    // Generate access token mới
                    String accessToken = jwtService.generateToken(extraClaims, userDetails);
                    // Add refresh token to blacklist
                    jwtService.blacklistToken(refreshToken);
                    // Generate new refresh-token
                    String newRefreshToken = jwtService.generateRefreshToken(userDetails);
                    AuthenticationResponse authResponse = AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(newRefreshToken)
                            .tokenType(tokenPrefix)
                            .expiresIn(jwtExpiration / 1000)
                            .build();

                    return authResponse;
                }
            }
            return null;
        } catch (Exception e) {
            log.error("Error refreshing token: ", e);
            return null;
        }
    }

    @Override
    public LoginResponse login(AuthenticationRequest request) {
        try {
            AuthenticationResponse authResponse = authenticate(request);
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
            if(user.getStatus() != Status.ACTIVE){
                throw new UnauthorizedException("Account is not active. Please verify your email.");
            }
            return LoginResponse.builder()
                    .accessToken(authResponse.getAccessToken())
                    .refreshToken(authResponse.getRefreshToken())
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .role(user.getRole())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest request) {
        try {
            // Validate OTP trước khi reset password (và xóa OTP sau khi thành công)
            boolean isOtpValid = emailService.validateOTPForPasswordReset(request.getEmail(), request.getOtp());
            if (!isOtpValid) {
                log.warn("Invalid OTP provided for password reset: email={}, otp={}", request.getEmail(), request.getOtp());
                return false;
            }

            Optional<User> user = userRepository.findByEmail(request.getEmail());
            if (user.isPresent()) {
                User existingUser = user.get();
                existingUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
                userRepository.save(existingUser);
                log.info("Password reset successfully for email: {}", request.getEmail());
                return true;
            } else {
                log.warn("User not found for password reset: email={}", request.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to reset password for user with email: {}", request.getEmail(), e);
            throw new UnauthorizedException("Failed to reset password for user with email: " + request.getEmail());
        }
        return false;
    }

    @Override
    public AuthenticationResponse registerDoctor(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        Doctor user = new Doctor();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.DOCTOR);
        user.setStatus(Status.ACTIVE);

        // Save user
        doctorRepository.save(user);

        // Generate JWT token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("email", user.getEmail());


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(extraClaims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Return authentication response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenPrefix)
                .expiresIn(jwtExpiration / 1000)
                .build();
    }

    @Override
    public AuthenticationResponse registerAdmin(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateUserException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        Doctor user = new Doctor();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.ADMIN);
        user.setStatus(Status.ACTIVE);

        // Save user
        doctorRepository.save(user);

        // Generate JWT token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", user.getRole().name());
        extraClaims.put("userId", user.getUserId());
        extraClaims.put("email", user.getEmail());


        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtService.generateToken(extraClaims, userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        // Return authentication response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType(tokenPrefix)
                .expiresIn(jwtExpiration / 1000)
                .build();
    }

    @Override
    public <T> T getMe(HttpServletRequest request, Class<T> clazz) {
        try {
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            // Kiểm tra header Authorization
            if (authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
                return null;
            }

            final String jwt = authHeader.substring(tokenPrefix.length() + 1);
            final String userEmail = jwtService.extractUsername(jwt);

            User user = userRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new UserNotFoundException("User not found"));

//            if (clazz.equals(Object.class)) {
//                if (user.getRole().equals(Role.DOCTOR)) {
//                    return clazz.cast(doctorService.getDoctorById(user.getUserId()));
//                } else if (user.getRole().equals(Role.PATIENT)) {
//                    return clazz.cast(user);
//                }
//            }

            if (user.getRole().equals(Role.DOCTOR)) {
                DoctorResponse doctorResponse = doctorService.getDoctorById(user.getUserId());
                return clazz.cast(doctorResponse);
            } else if (user.getRole().equals(Role.PATIENT)) {
                GetPatientResponse patientResponse = patientService.getPatientById(user.getUserId());
                return clazz.cast(patientResponse);
            }
            return null;
        } catch (Exception e) {
            log.error("Error occurred in getMe method: ", e);
            throw e;
        }
    }

    @Override
    public Boolean logout(HttpServletRequest request,String refreshToken) {
        try{
            final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            // Kiểm tra header Authorization
            if (authHeader == null || !authHeader.startsWith(tokenPrefix + " ")) {
                return false;
            }

            final String jwt = authHeader.substring(tokenPrefix.length() + 1);
            jwtService.blacklistToken(jwt);
            jwtService.blacklistToken(refreshToken);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

}