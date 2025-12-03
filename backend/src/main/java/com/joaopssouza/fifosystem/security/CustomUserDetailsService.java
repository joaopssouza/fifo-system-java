package com.joaopssouza.fifosystem.security;

import com.joaopssouza.fifosystem.domain.entity.User;
import com.joaopssouza.fifosystem.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

        private final UserRepository userRepository;

        @Override
        public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userRepository.findByUsername(username)
                                .orElseThrow(() -> new UsernameNotFoundException(
                                                "Usuário não encontrado: " + username));

                // Converte o Role do banco para Authority do Spring
                Set<GrantedAuthority> authorities = Collections.singleton(
                                new SimpleGrantedAuthority("ROLE_" + user.getRole().getName().toUpperCase()));

                return new org.springframework.security.core.userdetails.User(
                                user.getUsername(),
                                user.getPasswordHash(),
                                authorities);
        }
}