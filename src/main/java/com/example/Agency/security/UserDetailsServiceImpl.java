package com.example.Agency.security;


import com.example.Agency.model.User;
import com.example.Agency.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

//    @Override
//    public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {
//        Optional<User> retailerOptional = userRepository.findByMobileNumber(mobileNumber);
//        if (retailerOptional.isPresent()) {
//            return new org.springframework.security.core.userdetails.User(
//                    retailerOptional.get().getMobileNumber(),
//                    retailerOptional.get().getUserId(),
//                    new ArrayList<>()
//            );
//        } else {
//            throw new UsernameNotFoundException("Retailer not found");
//        }
//    }

    @Override
    public UserDetails loadUserByUsername(String mobileNumber) throws UsernameNotFoundException {
        User user = userRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole());

        return new org.springframework.security.core.userdetails.User(
                user.getMobileNumber(),
                user.getPasswordHash(),
                Collections.singletonList(authority));
    }

    // Method to find retailer by mobile number
    public Optional<User> findByMobileNo(String mobileNo) {
        return userRepository.findByMobileNumber(mobileNo);
    }
}
