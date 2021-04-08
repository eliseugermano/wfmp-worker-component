package br.health.workflow.controller.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;

@ControllerAdvice(annotations = RestController.class)
public class ExceptionHandler extends ResponseEntityExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(value = { Exception.class })
    protected ResponseEntity<Object> handleConflict(RuntimeException ex) {
        HttpStatus httpStatusCode = HttpStatus.INTERNAL_SERVER_ERROR;

        return createResponse(ex.getMessage(), httpStatusCode);
    }

    private ResponseEntity<Object> createResponse(String message, HttpStatus httpStatusCode) {
        HashMap<String, String> response = new HashMap<>();
        response.put("message", message);
        return new ResponseEntity<>(response, httpStatusCode);
    }

}
