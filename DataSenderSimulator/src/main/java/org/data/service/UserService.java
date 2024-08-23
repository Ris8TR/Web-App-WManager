package org.data.service;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.data.dto.NewUserDto;
import org.json.JSONObject;

import java.util.Random;

public class UserService {

    private static final String USER_CREATION_URL = "http://192.168.15.34:8010/v1/users";
    private static final String LOGIN_URL = "http://192.168.15.34:8010/v1/login/user";

    public NewUserDto createDummyUser(int userId, Random random) {
        NewUserDto newUser = new NewUserDto();
        newUser.setEmail("user" + userId + "@example.com");
        newUser.setFirstName("FirstName" + userId);
        newUser.setLastName("LastName" + userId);
        newUser.setPassword("password" + userId);
        newUser.setSensorPassword("sensorPassword" + userId);
        System.out.println(newUser.toString());
        return newUser;
    }

    public String sendNewUser(NewUserDto newUser) {
        String userId = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(USER_CREATION_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            String userJson = String.format(
                    """
                    {
                      "email": "%s",
                      "firstName": "%s",
                      "lastName": "%s",
                      "password": "%s",
                      "sensorPassword": "%s"
                    }""",
                    newUser.getEmail(), newUser.getFirstName(), newUser.getLastName(),
                    newUser.getPassword(), newUser.getSensorPassword()
            );

            StringEntity entity = new StringEntity(userJson, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    System.out.println("User created successfully: " + newUser.getEmail());
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    userId = jsonResponse.getString("id");
                } else {
                    System.err.println("Failed to create user: " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userId;
    }

    public String loginUser(NewUserDto newUser) {
        String token = null;
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(LOGIN_URL);
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");

            String loginJson = String.format(
                    """
                    {
                      "email": "%s",
                      "password": "%s"
                    }""",
                    newUser.getEmail(), newUser.getPassword()
            );

            StringEntity entity = new StringEntity(loginJson, ContentType.APPLICATION_JSON);
            post.setEntity(entity);

            try (CloseableHttpResponse response = client.execute(post)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                if (response.getStatusLine().getStatusCode() == 200) {
                    token = parseTokenFromResponse(responseBody);
                    System.out.println("Login successful for user: " + newUser.getEmail());
                } else {
                    System.err.println("Failed to login: " + response.getStatusLine().getStatusCode());
                    System.err.println("Response: " + responseBody);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    private String parseTokenFromResponse(String responseBody) {
        return new JSONObject(responseBody).getString("token");
    }
}
