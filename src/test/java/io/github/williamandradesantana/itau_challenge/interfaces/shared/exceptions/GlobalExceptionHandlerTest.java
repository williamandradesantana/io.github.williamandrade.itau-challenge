package io.github.williamandradesantana.itau_challenge.interfaces.shared.exceptions;

import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidCredentialsException;
import io.github.williamandradesantana.itau_challenge.application.auth.exceptions.InvalidRefreshTokenException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.RequiredObjectIsNullException;
import io.github.williamandradesantana.itau_challenge.application.shared.exceptions.ResourceNotFoundException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.InvalidPasswordException;
import io.github.williamandradesantana.itau_challenge.domain.auth.user.exceptions.UserNameAlreadyExistsException;
import io.github.williamandradesantana.itau_challenge.domain.shared.exceptions.InvalidPaginationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new ServletWebRequest(new MockHttpServletRequest("GET", "/api/test"));
    }

    @SuppressWarnings("unused")
    private void anyEndpoint(UUID id) {
    }

    private MethodParameter methodParameter() throws NoSuchMethodException {
        return new MethodParameter(GlobalExceptionHandlerTest.class.getDeclaredMethod("anyEndpoint", UUID.class), 0);
    }

    private void assertResponse(ResponseEntity<ExceptionResponse> response, int expectedStatus, String expectedMessage) {
        assertEquals(expectedStatus, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(expectedMessage, response.getBody().message());
        assertNotNull(response.getBody().timestamp(), () -> "the response must carry a timestamp");
        assertEquals("uri=/api/test", response.getBody().details());
    }

    @Test
    @DisplayName("Test: required object is null should be a bad request")
    void test_RequiredObjectIsNullShouldBeABadRequest() {
        RequiredObjectIsNullException exception = new RequiredObjectIsNullException();

        assertResponse(handler.handleRequiredObjectIsNull(exception, request), 400, exception.getMessage());
    }

    @Test
    @DisplayName("Test: a domain rule violation should be a bad request")
    void test_ADomainRuleViolationShouldBeABadRequest() {
        assertResponse(handler.handleDomain(new InvalidPasswordException("The password must have at least 8 characters"), request),
            400, "The password must have at least 8 characters");
        assertResponse(handler.handleDomain(new InvalidPaginationException("The size must be between 1 and 100"), request),
            400, "The size must be between 1 and 100");
    }

    @Test
    @DisplayName("Test: a user name already in use should be a conflict")
    void test_AUserNameAlreadyInUseShouldBeAConflict() {
        assertResponse(handler.handleUserNameAlreadyExists(new UserNameAlreadyExistsException("UserName already exists: bob"), request),
            409, "UserName already exists: bob");
    }

    @Test
    @DisplayName("Test: resource not found should be a not found")
    void test_ResourceNotFoundShouldBeANotFound() {
        assertResponse(handler.handleNotFound(new ResourceNotFoundException("User not found"), request), 404, "User not found");
    }

    @Test
    @DisplayName("Test: invalid credentials and invalid refresh token should be unauthorized")
    void test_InvalidCredentialsAndInvalidRefreshTokenShouldBeUnauthorized() {
        InvalidCredentialsException credentials = new InvalidCredentialsException();
        InvalidRefreshTokenException refresh = new InvalidRefreshTokenException();

        assertResponse(handler.handleInvalidCredentials(credentials, request), 401, credentials.getMessage());
        assertResponse(handler.handleInvalidRefreshToken(refresh, request), 401, refresh.getMessage());
    }

    @Test
    @DisplayName("Test: access denied should be forbidden and must not echo the internal message")
    void test_AccessDeniedShouldBeForbiddenAndMustNotEchoTheInternalMessage() {
        assertResponse(handler.handleAccessDenied(new AccessDeniedException("internal authorization detail"), request),
            403, "Access denied");
    }

    @Test
    @DisplayName("Test: bean validation errors should list each invalid field")
    void test_BeanValidationErrorsShouldListEachInvalidField() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registerUserRequest");
        bindingResult.addError(new FieldError("registerUserRequest", "userName", "must not be blank"));
        bindingResult.addError(new FieldError("registerUserRequest", "password", "must not be blank"));

        ResponseEntity<ExceptionResponse> response =
            handler.handleBeanValidation(new MethodArgumentNotValidException(methodParameter(), bindingResult), request);

        assertResponse(response, 400, "userName: must not be blank; password: must not be blank");
    }

    @Test
    @DisplayName("Test: a validation failure without field errors should still be a bad request")
    void test_AValidationFailureWithoutFieldErrorsShouldStillBeABadRequest() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "registerUserRequest");

        ResponseEntity<ExceptionResponse> response =
            handler.handleBeanValidation(new MethodArgumentNotValidException(methodParameter(), bindingResult), request);

        assertResponse(response, 400, "Validation failed");
    }

    @Test
    @DisplayName("Test: a parameter of the wrong type should name the parameter")
    void test_AParameterOfTheWrongTypeShouldNameTheParameter() throws Exception {
        MethodArgumentTypeMismatchException exception =
            new MethodArgumentTypeMismatchException("abc", UUID.class, "id", methodParameter(), new IllegalArgumentException("bad uuid"));

        assertResponse(handler.handleTypeMismatch(exception, request), 400, "Invalid value for parameter 'id'");
    }

    @Test
    @DisplayName("Test: an unreadable body should be a bad request and must not leak the parser message")
    void test_AnUnreadableBodyShouldBeABadRequestAndMustNotLeakTheParserMessage() {
        HttpMessageNotReadableException exception =
            new HttpMessageNotReadableException("Unexpected character at line 3 column 7 (parser internals)", new MockHttpInputMessage(new byte[0]));

        assertResponse(handler.handleUnreadableBody(exception, request), 400, "Malformed or missing request body");
    }

    @Test
    @DisplayName("Test: exceptions of Spring MVC should keep their own status")
    void test_ExceptionsOfSpringMvcShouldKeepTheirOwnStatus() {
        ResponseEntity<ExceptionResponse> response = handler.handleAll(new HttpRequestMethodNotSupportedException("PUT"), request);

        assertEquals(405, response.getStatusCode().value(), () -> "a wrong HTTP method must not become a 500");
        assertNotNull(response.getBody().message());
    }

    @Test
    @DisplayName("Test: an unexpected exception should be a generic internal server error that hides the cause")
    void test_AnUnexpectedExceptionShouldBeAGenericInternalServerErrorThatHidesTheCause() {
        ResponseEntity<ExceptionResponse> response =
            handler.handleAll(new IllegalStateException("password authentication failed for user postgres"), request);

        assertResponse(response, 500, "Internal server error");
        assertFalse(response.getBody().message().contains("postgres"), () -> "internal details must never reach the client");
    }
}
