package com.hmdp.utils;

import com.hmdp.dto.UserDTO;

import java.util.Random;

public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        if(tl.get() == null){
            UserDTO userDTO = new UserDTO();
            long mockId = Math.abs(new Random().nextLong());
            userDTO.setId(mockId);
//            userDTO.setId(1L);
            userDTO.setNickName("ADMIN");
            return userDTO;
        }
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
