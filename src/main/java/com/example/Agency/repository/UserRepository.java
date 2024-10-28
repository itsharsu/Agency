package com.example.Agency.repository;

import com.example.Agency.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,String> {
      Optional<User> findByMobileNumber(String mobileNo);

    //  Optional<User> findByUserId(String userId);
}
