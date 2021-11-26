package com.example.api.controllers;

import com.example.api.controllers.payload.LoginRequest;
import com.example.api.controllers.payload.UserList;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author oleg Turchin
 */
@SpringBootTest()
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    @Order(1)
    void preparedUsersAndTokens() {
        // add user operator
        addOperatorForTests();
        // add user analyst
        addAnalystForTests();
    }

    @SneakyThrows
    @Test
    @Order(2)
    void getAllUsers() {
        String adminToken = getTokenForAdmin();
        String operatorToken = getTokenForOperator();
        String analystToken = getTokenForAnalyst();

        MvcResult content =mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        assertTrue(rootNode.isArray());
        assertTrue(rootNode.size() > 0);

        mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + operatorToken))
                .andExpect(status().is4xxClientError())
                .andReturn();
        mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().is4xxClientError())
                .andReturn();
    }

    @SneakyThrows
    @Test
    @Order(3)
    void getUser() {
        String token = getTokenForAdmin();
        MvcResult content =mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        assertTrue(rootNode.isArray());
        assertTrue(rootNode.size() > 0);
        JsonNode userNode = rootNode.get(0);
        long id = userNode.get("id").asLong();
        long idOperator = -1, idAnalyst = -1;
        for (int i=0; i<rootNode.size(); i++) {
            userNode = rootNode.get(i);
            if (userNode.get("login").asText().equals("operator_for_test")) {
                idOperator = userNode.get("id").asLong();
            }
            if (userNode.get("login").asText().equals("analyst_for_test")) {
                idAnalyst = userNode.get("id").asLong();
            }
        }
        content =mvc.perform(get("/api/users/"+id)
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn();
        rootNode = mapper.readTree(content.getResponse().getContentAsString());
        assertTrue(rootNode.has("id"));
        assertNotNull(rootNode.get("id"));

        // get user without admin role
        String tokenOperator = getTokenForOperator();
        mvc.perform(get("/api/users/"+id)
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenOperator))
                .andExpect(status().is4xxClientError())
                .andReturn();
        String tokenAnalyst = getTokenForAnalyst();
        mvc.perform(get("/api/users/"+id)
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenAnalyst))
                .andExpect(status().is4xxClientError())
                .andReturn();

        // get non existing user
        mvc.perform(get("/api/users/-1")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().is4xxClientError())
                .andDo(print())
                .andReturn();

        // get user on behalf of owner
        System.out.println("--- id operator --: " + idOperator);
        mvc.perform(get("/api/users/"+idOperator)
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenOperator))
                .andExpect(status().isOk())
                .andReturn();
        System.out.println("--- id analyst --: " + idAnalyst);
        mvc.perform(get("/api/users/"+idAnalyst)
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenAnalyst))
                .andExpect(status().isOk())
                .andReturn();
    }

    @SneakyThrows
    @Test
    @Order(4)
    void createUser() {
        String tokenAdmin = getTokenForAdmin();
        String tokenOperator = getTokenForOperator();
        String tokenAnalyst = getTokenForAnalyst();
        String newUserJsonStr = "{\n" +
                "  \"id\": null,\n" +
                "  \"login\": \"test-user-this-test\",\n" +
                "  \"password\": \"test-user-this-test\",\n" +
                "  \"fullName\": \"test-user-this-test\",\n" +
                "  \"email\": \"test-user-this-test\",\n" +
                "  \"userRole\": \"ROLE_OPERATOR\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .content(newUserJsonStr))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenAdmin)
                        .content(newUserJsonStr))
                .andExpect(status().is4xxClientError())
                .andReturn();

        newUserJsonStr = "{\n" +
                "  \"id\": null,\n" +
                "  \"login\": \"test-user-this-test1\",\n" +
                "  \"password\": \"test-user-this-test1\",\n" +
                "  \"fullName\": \"test-user-this-test1\",\n" +
                "  \"email\": \"test-user-this-test1\",\n" +
                "  \"userRole\": \"ROLE_OPERATOR\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenOperator)
                        .content(newUserJsonStr))
                .andExpect(status().is4xxClientError())
                .andReturn();
        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + tokenAnalyst)
                        .content(newUserJsonStr))
                .andExpect(status().is4xxClientError())
                .andReturn();
        deleteUserForTest("test-user-this-test");
    }

    @SneakyThrows
    @Test
    @Order(5)
    void updateUser() {
        // update operator by admin user
        String adminToken = getTokenForAdmin();
        String operatorToken = getTokenForOperator();
        String analystToken = getTokenForAnalyst();
        // get userList
        MvcResult content =mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<UserList> userList = mapper.readValue(content.getResponse().getContentAsString(),
                new TypeReference<List<UserList>>(){});
        assertTrue(userList.size() > 0);
        // find user
        UserList ul = userList.stream()
                .filter(userList1 -> userList1.getLogin().equals("operator_for_test"))
                .findFirst()
                .orElse(null);
        assertNotNull(ul);
        assertNotNull(ul.getId());

        String body = "{\n" +
                "  \"id\": " + ul.getId().toString() + ",\n" +
                "  \"login\": \"operator_for_test\",\n" +
                "  \"password\": \"operator_for_test\",\n" +
                "  \"fullName\": \"operator for test 123\",\n" +
                "  \"email\": \"operator_for_test@zz.zz\",\n" +
                "  \"userRole\": \"ROLE_OPERATOR\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        mvc.perform(put("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        // update not exist user by admin user
        body = "{\n" +
                "  \"id\": -1,\n" +
                "  \"login\": \"operator_for_test\",\n" +
                "  \"password\": \"operator_for_test\",\n" +
                "  \"fullName\": \"operator for test 123\",\n" +
                "  \"email\": \"operator_for_test@zz.zz\",\n" +
                "  \"userRole\": \"ROLE_OPERATOR\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        mvc.perform(put("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(body))
                .andExpect(status().is4xxClientError())
                .andReturn();

        // update operator by not admin user and not owner
        body = "{\n" +
                "  \"id\": " + ul.getId().toString() + ",\n" +
                "  \"login\": \"operator_for_test\",\n" +
                "  \"password\": \"operator_for_test\",\n" +
                "  \"fullName\": \"operator for test 123\",\n" +
                "  \"email\": \"operator_for_test@zz.zz\",\n" +
                "  \"userRole\": \"ROLE_OPERATOR\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        mvc.perform(put("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + analystToken)
                        .content(body))
                .andExpect(status().is4xxClientError())
                .andReturn();
        // update operator by not admin user and owner
        mvc.perform(put("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + operatorToken)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
    }

    @SneakyThrows
    @Test
    @Order(6)
    void setUserPassword() {
        String adminToken = getTokenForAdmin();
        String analystToken = getTokenForAnalyst();
        // change operator password on behalf of admin
        // get userList
        MvcResult content =mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<UserList> userList = mapper.readValue(content.getResponse().getContentAsString(),
                new TypeReference<List<UserList>>(){});
        assertTrue(userList.size() > 0);
        // find user
        UserList ul = userList.stream()
                .filter(userList1 -> userList1.getLogin().equals("operator_for_test"))
                .findFirst()
                .orElse(null);
        assertNotNull(ul);
        assertNotNull(ul.getId());

        // change operator password on behalf of owner
        mvc.perform(put("/api/users/setPassword/" + ul.getId() + "?newPassword=54321")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        // change operator empty password on behalf of owner
        mvc.perform(put("/api/users/setPassword/" + ul.getId() + "?newPassword=")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is4xxClientError())
                .andReturn();
        // change operator password on behalf of analyst
        mvc.perform(put("/api/users/setPassword/" + ul.getId() + "?newPassword=54321")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().is4xxClientError())
                .andReturn();
        mvc.perform(put("/api/users/setPassword/" + ul.getId() + "?newPassword=operator_for_test")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

    }

    @SneakyThrows
    @Test
    @Order(7)
    void deleteUser() {
        // delete operator by admin user
        String adminToken = getTokenForAdmin();
        String analystToken = getTokenForAnalyst();
        // get userList
        MvcResult content =mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<UserList> userList = mapper.readValue(content.getResponse().getContentAsString(),
                new TypeReference<List<UserList>>(){});
        assertTrue(userList.size() > 0);
        // find user
        UserList ul = userList.stream()
                .filter(userList1 -> userList1.getLogin().equals("operator_for_test"))
                .findFirst()
                .orElse(null);
        assertNotNull(ul);
        assertNotNull(ul.getId());
        // delete user
        mvc.perform(delete("/api/users/" + ul.getId())
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();

        // delete not exist user by admin
        mvc.perform(delete("/api/users/-1")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is5xxServerError())
                .andReturn();

        // delete analyst by analyst user
        ul = userList.stream()
                .filter(userList1 -> userList1.getLogin().equals("analyst_for_test"))
                .findFirst()
                .orElse(null);
        assertNotNull(ul);
        assertNotNull(ul.getId());
        mvc.perform(delete("/api/users/" + ul.getId())
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + analystToken))
                .andExpect(status().is4xxClientError())
                .andReturn();
        deleteUserForTest("analyst_for_test");
    }

    @SneakyThrows
    private String getTokenForAdmin () {
        String token;
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("admin");
        loginRequest.setPassword("admin");
        String body = mapper.writeValueAsString(loginRequest);
        MvcResult content = mvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        assertTrue(rootNode.has("id"));
        assertNotNull(rootNode.get("id"));
        assertTrue(rootNode.has("token"));
        assertNotNull(rootNode.get("token"));
        assertTrue(rootNode.has("username"));
        assertEquals("admin", rootNode.get("username").asText());
        assertTrue(rootNode.has("email"));
        assertTrue(rootNode.has("roles"));
        assertTrue(rootNode.get("roles").isArray());
        JsonNode roles = rootNode.get("roles");
        assertEquals("ROLE_ADMIN", roles.get(0).asText());
        token = rootNode.get("token").asText();
        return token;
    }

    @SneakyThrows
    private void addOperatorForTests() {
        String operatorForTest = "{\n" +
                "  \"id\": null,\n" +
                "  \"login\": \"operator_for_test\",\n" +
                "  \"password\": \"operator_for_test\",\n" +
                "  \"fullName\": \"operator for test\",\n" +
                "  \"email\": \"operator_for_test@zz.zz\",\n" +
                "  \"userRole\": \"ROLE_OPERATOR\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        String adminToken = getTokenForAdmin();
        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(operatorForTest))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @SneakyThrows
    private String getTokenForOperator () {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("operator_for_test");
        loginRequest.setPassword("operator_for_test");
        String body = mapper.writeValueAsString(loginRequest);
        MvcResult content = mvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        return rootNode.get("token").asText();
    }

    @SneakyThrows
    private void addAnalystForTests() {
        String operatorForTest = "{\n" +
                "  \"id\": null,\n" +
                "  \"login\": \"analyst_for_test\",\n" +
                "  \"password\": \"analyst_for_test\",\n" +
                "  \"fullName\": \"analyst for test\",\n" +
                "  \"email\": \"analyst_for_test@zz.zz\",\n" +
                "  \"userRole\": \"ROLE_ANALYST\",\n" +
                "  \"lang\": \"EN\",\n" +
                "  \"theme\": \"AUTO\",\n" +
                "  \"active\": true,\n" +
                "  \"tokenExp\": 86400000,\n" +
                "  \"url_avatar\": \"\"\n" +
                "}";
        String adminToken = getTokenForAdmin();
        mvc.perform(post("/api/users")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + adminToken)
                        .content(operatorForTest))
                .andExpect(status().is2xxSuccessful())
                .andReturn();
    }

    @SneakyThrows
    private String getTokenForAnalyst () {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("analyst_for_test");
        loginRequest.setPassword("analyst_for_test");
        String body = mapper.writeValueAsString(loginRequest);
        MvcResult content = mvc.perform(post("/api/auth/signin")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode rootNode = mapper.readTree(content.getResponse().getContentAsString());
        return rootNode.get("token").asText();
    }

    @SneakyThrows
    private void deleteUserForTest(String login) {
        String adminToken = getTokenForAdmin();
        // get userList
        MvcResult content =mvc.perform(get("/api/users")
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
        List<UserList> userList = mapper.readValue(content.getResponse().getContentAsString(),
                new TypeReference<List<UserList>>(){});
        assertTrue(userList.size() > 0);
        // find user
        UserList ul = userList.stream()
                .filter(userList1 -> userList1.getLogin().equals(login))
                .findFirst()
                .orElse(null);
        assertNotNull(ul);
        assertNotNull(ul.getId());
        // delete user
        mvc.perform(delete("/api/users/" + ul.getId())
                        .contentType("application/json")
                        .accept("application/json")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andReturn();
    }
}