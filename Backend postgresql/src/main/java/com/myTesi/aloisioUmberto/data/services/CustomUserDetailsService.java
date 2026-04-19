package com.myTesi.aloisioUmberto.data.services;

import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository; // Il tuo DAO o repository JPA

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cerca l'utente nel database usando l'email
        Optional<com.myTesi.aloisioUmberto.data.entities.User> userEntityOpt = userRepository.findUserByEmail(email);

        if (userEntityOpt.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        com.myTesi.aloisioUmberto.data.entities.User userEntity = userEntityOpt.get();

        // Ottieni il ruolo dell'utente e mappalo in GrantedAuthority
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(userEntity.getRole().name())); // "ROLE_" viene aggiunto automaticamente se il tuo enum lo include

        // Restituisci i dettagli dell'utente con i ruoli e la password
        return new User(userEntity.getEmail(), userEntity.getPassword(), authorities);
    }
}
