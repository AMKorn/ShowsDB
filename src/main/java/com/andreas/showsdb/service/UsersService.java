package com.andreas.showsdb.service;

import com.andreas.showsdb.model.User;
import com.andreas.showsdb.repository.UsersRepository;
import com.andreas.showsdb.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsersService implements UserDetailsService {
    private final UsersRepository userRepository;

    public UsersService(UsersRepository userRepository) {
        this.userRepository = userRepository;
    }

    private static final Logger logger = LoggerFactory.getLogger(UsersService.class);

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        logger.debug("Entering in loadUserByUsername Method...");
        User user = userRepository.findByUsername(username);
        if (user == null) {
            logger.error("Username not found: " + username);
            throw new UsernameNotFoundException("could not find user..!!");
        }
        logger.info("User Authenticated Successfully");
        return new CustomUserDetails(user);
    }
}
