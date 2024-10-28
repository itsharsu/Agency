package com.example.Agency.service;

import com.example.Agency.dto.ApiResponse;
import com.example.Agency.model.User;
import com.example.Agency.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public ApiResponse<List<User>> getAllUsers(){
        List<User> users = userRepository.findAll();
        return new ApiResponse<>(true,"Users retrieved successfully",users,null);
    }

    public ApiResponse<Optional<User>> getUserById(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isPresent()){
            return new ApiResponse<>(true,"User found!",user,null);
        }
        return new ApiResponse<>(false,"User not Found",null,null);
    }

    public ApiResponse<String> deleteUser(String userID) {
        if (userRepository.existsById(userID)) {
            userRepository.deleteById(userID); // Delete the user
            return new ApiResponse<>(true, "Successfully Deleted!", null, null);
        } else {
            return new ApiResponse<>(false, "Unable to delete: User not found", null, null);
        }
    }


    public ApiResponse<User> updateUser(String userId, User updatedUser) {
        Optional<User> existingUser = userRepository.findById(userId);
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            // Update user fields as needed
            user.setUserName(updatedUser.getUserName());
            user.setShopName(updatedUser.getShopName());
            user.setAddress(updatedUser.getAddress());
            // You might skip the password field if you don't want to update it here

            userRepository.save(user); // Save updated user to database
            return new ApiResponse<>(true, "User updated successfully", user, null);
        } else {
            return new ApiResponse<>(false, "User not found", null, null);
        }
    }


}
