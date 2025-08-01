package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.AuthenticationRequest;
import fit.iuh.student.userservice.dtos.requests.RegisterRequest;
import fit.iuh.student.userservice.dtos.responses.AuthenticationResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Service interface for authentication operations
 */
public interface AuthenticationService {
    /**
     * Register a new user
     * @param request registration request
     * @return authentication response with JWT token
     */
    AuthenticationResponse register(RegisterRequest request);

    /**
     * Authenticate a user
     * @param request authentication request
     * @return authentication response with JWT token
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Refresh JWT token
     * @param request HTTP request
     * @param response HTTP response
     * @throws IOException if an I/O error occurs
     */
    void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException;
}