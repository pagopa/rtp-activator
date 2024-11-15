package it.gov.pagopa.rtp.activator;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;

// Controller to play with role and authorization
// TODO: remove me
@RestController
public class PlaygroundController {

    @PreAuthorize("hasRole('mil-auth-admin')")
    @GetMapping("/test")
    public Mono<ResponseEntity<String>> trySomething(
            Principal principal
    ) {
        return Mono.just(
                ResponseEntity.ok("Ciao " + principal.getName())
        );
    }

    @PreAuthorize("hasRole('mil-auth-admin')")
    @GetMapping("/test2")
    public Mono<ResponseEntity<String>> trySomething2(
            Authentication authentication
    ) {
        return Mono.just(
                ResponseEntity.ok("Ciao " + authentication.getName() + " " + authentication.getAuthorities())
        );
    }

}
