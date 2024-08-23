package com.myTesi.aloisioUmberto.data.services;


import com.myTesi.aloisioUmberto.config.EmailSender;
import com.myTesi.aloisioUmberto.config.JwtTokenProvider;
import com.myTesi.aloisioUmberto.data.dao.UserRepository;
import com.myTesi.aloisioUmberto.data.entities.User;
import com.myTesi.aloisioUmberto.data.services.interfaces.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userDao;
    private final ModelMapper modelMapper = new ModelMapper();
    private final EmailSender emailSender;
    private final JwtTokenProvider jwtTokenProvider;


    @Override
    public Object loginUser(HttpServletRequest req, HttpServletResponse resp, Map<String, String> requestBody) {
        String email = requestBody.get("email");
        String password = requestBody.get("password");
        Optional<User> user = userDao.findUserByEmail(email);
        if (user.isPresent() && BCrypt.checkpw(password, user.get().getPassword())) {
            SecurityContextHolder.getContext().setAuthentication(createAuthentication(email, password));
            String token = jwtTokenProvider.generateUserToken(user);
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            HttpSession session = req.getSession();
            resp.addCookie(new Cookie("sessionId", session.getId()));
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }

    }


    @Override
    public Object resetPass(String email) {
        String type=null;
        if (userDao.findUserByEmail(email).isPresent())
            type= "user";
        if (type!=null) {
            String token = jwtTokenProvider.generateResetPassToken(email,type);
            CompletableFuture.runAsync(() -> {
                emailSender.sendSimpleEmail(email, "Reset Password", "Ciao, sembra che tu abbia dimenticato la tua password. \n" +
                        "Clicca qui per cambiarla: " + "http://192.168.15.34:4200/reset/:" + token);
            });
            return ResponseEntity.ok(token);
        }
        return null;

    }

    @Override
    public Boolean savePassword(Map<String, String> requestBody) {
        String pass = requestBody.get("password");
        String token = requestBody.get("token");
        token= token.substring(1);
        if (jwtTokenProvider.validateToken(token)) {
            if (jwtTokenProvider.getTypeFromToken(token).equals("user")) {
                Optional<User> user = userDao.findUserByEmail(jwtTokenProvider.getEmailFromUserToken(token));
                user.get().setPassword(BCrypt.hashpw(pass, BCrypt.gensalt(10)));
                userDao.save(user.get());
                return true;
            }
        }
        return false;
    }

    private Authentication createAuthentication(String email, String password) {
        return new UsernamePasswordAuthenticationToken(email, password, Collections.emptyList());
    }



}
