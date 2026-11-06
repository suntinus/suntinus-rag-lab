package com.suntinus.rag;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.Map;
@RestControllerAdvice
public class Errors {
    @ExceptionHandler({IllegalArgumentException.class,java.time.format.DateTimeParseException.class,MethodArgumentNotValidException.class})
    ResponseEntity<?> invalid(Exception ignored){return ResponseEntity.badRequest().body(Map.of("error","Invalid request. Check fields, date, backend and vector dimensions."));}
    @ExceptionHandler(Exception.class)
    ResponseEntity<?> unavailable(Exception error){
        org.slf4j.LoggerFactory.getLogger(Errors.class).warn("RAG operation failed: {}",error.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("error","Operation failed. Check database, local models and configuration; no partial document was committed."));
    }
}
