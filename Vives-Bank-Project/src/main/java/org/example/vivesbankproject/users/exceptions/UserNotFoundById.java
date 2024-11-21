package org.example.vivesbankproject.users.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundById extends UserException {
    public UserNotFoundById(String id) {
        super("Usuario con id '" + id + "' no encontrado");
    }
}