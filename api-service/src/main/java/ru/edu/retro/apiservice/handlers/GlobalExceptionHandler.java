package ru.edu.retro.apiservice.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.edu.retro.apiservice.exceptions.AuthException;
import ru.edu.retro.apiservice.exceptions.BadRequestException;
import ru.edu.retro.apiservice.exceptions.EntityExistsException;
import ru.edu.retro.apiservice.exceptions.EntityNotFoundException;
import ru.edu.retro.apiservice.exceptions.ForbiddenException;
import ru.edu.retro.apiservice.exceptions.InvalidCredentialsException;
import ru.edu.retro.apiservice.exceptions.InvalidInviteTokenException;
import ru.edu.retro.apiservice.exceptions.InvalidTokenException;
import ru.edu.retro.apiservice.models.dto.responses.ErrorResponse;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handle(EntityNotFoundException e) {
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), e.getMessage());
    }

    @ExceptionHandler(exception = AuthException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handle(AuthException e) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    @ExceptionHandler(exception = InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handle(InvalidCredentialsException e) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    @ExceptionHandler(exception = InvalidTokenException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handle(InvalidTokenException e) {
        return new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
    }

    @ExceptionHandler(exception = {BadRequestException.class, EntityExistsException.class, InvalidInviteTokenException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handle(RuntimeException e) {
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.getMessage());
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handle(ForbiddenException e) {
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }


}
