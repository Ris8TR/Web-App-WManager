package com.myTesi.aloisioUmberto.data.services.interfaces;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public interface AuthService {

    Object loginUser(HttpServletRequest req, HttpServletResponse resp, Map<String, String> requestBody);
    Object resetPass(String email);
    Boolean savePassword(Map<String, String> requestBody);

    Boolean checkToken(String token);
}
