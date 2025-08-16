package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.AuthenticationRequest;
import fit.iuh.student.userservice.dtos.requests.RegisterRequest;
import fit.iuh.student.userservice.dtos.requests.ResetPasswordRequest;
import fit.iuh.student.userservice.dtos.responses.AuthenticationResponse;
import fit.iuh.student.userservice.dtos.responses.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Service interface for authentication operations
 */
public interface AuthenticationService {
    /**
     * Register a new user
     *
     * @param request registration request
     * @return authentication response with JWT token
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Authenticate a user
     *
     * @param request authentication request
     * @return authentication response with JWT token
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Refresh JWT token
     *
     * @param request  HTTP request
     * @param response HTTP response
     * @throws IOException if an I/O error occurs
     */
    AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;

    /**
     * Login
     *
     * @param request HTTP request
     * @return LoginResponse containing user details and JWT token
     */
    LoginResponse login(AuthenticationRequest request);

    /**
     * Reset password for user
     * @param request reset password request containing email and new password
     * @return true if successful, false otherwise
     */
    boolean resetPassword(ResetPasswordRequest request);

    AuthenticationResponse registerDoctor(RegisterRequest request);

    AuthenticationResponse registerAdmin(RegisterRequest request);

    <T> T getMe(HttpServletRequest request, Class<T> clazz);
}