package com.example.demo.config;

import com.example.demo.usuario.PasswordRestablecida;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class InvalidarSesionesPorPassword {

    private final SessionRegistry sessionRegistry;

    public InvalidarSesionesPorPassword(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void invalidar(PasswordRestablecida evento) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof UserDetails usuario
                    && usuario.getUsername().equalsIgnoreCase(evento.correo())) {

                sessionRegistry.getAllSessions(principal, false)
                        .forEach(sesion -> sesion.expireNow());
            }
        }
    }
}