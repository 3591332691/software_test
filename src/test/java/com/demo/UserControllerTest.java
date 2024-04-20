package com.demo;

import com.demo.controller.user.UserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testSignUp() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/signup"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("signup"));
    }

    @Test
    public void testLogin() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"));
    }

    @Test
    public void testValidLogin() throws Exception {
        String userID = "testUser";
        String password = "testPassword";
        User user = new User();
        // 设置期望的用户返回值
        when(userService.checkLogin(userID, password)).thenReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.post("/loginCheck.do")
                        .param("userID", userID)
                        .param("password", password))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("/index"));
    }

    @Test
    public void testInvalidLogin() throws Exception {
        String userID = "testUser";
        String password = "wrongPassword"; // 使用错误的密码

        // 设置期望的用户返回值为null，表示登录失败
        when(userService.checkLogin(userID, password)).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.post("/loginCheck.do")
                        .param("userID", userID)
                        .param("password", password))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("false")); // 预期返回值为"false"
    }

    @Test
    public void testValidRegistration() throws Exception {
        String userID = "testUser";
        String userName = "Test User";
        String password = "testPassword";
        String email = "test@example.com";
        String phone = "1234567890";

        mockMvc.perform(MockMvcRequestBuilders.post("/register.do")
                        .param("userID", userID)
                        .param("userName", userName)
                        .param("password", password)
                        .param("email", email)
                        .param("phone", phone))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("login"));
    }

    @Test
    public void testMissingParameters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/register.do")
                        .param("userID", "testUser")
                        .param("password", "testPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().string("Missing parameters"));
    }
/*
    @Test
    public void testDuplicateRegistration() throws Exception {
        String userID = "existingUser";
        // 模拟UserService中已存在相同userID的情况
        when(userService.exists(userID)).thenReturn(true);

        mockMvc.perform(MockMvcRequestBuilders.post("/register.do")
                        .param("userID", userID)
                        .param("userName", "Test User")
                        .param("password", "testPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890"))
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.content().string("User already exists"));
    }


*/
@Test
public void testLogout() throws Exception {
    // 有效类测试：用户成功登出
    HttpSession session = mockMvc.perform(MockMvcRequestBuilders.get("/login"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.view().name("login"))
            .andReturn().getRequest().getSession();
    session.setAttribute("user", new User());

    mockMvc.perform(MockMvcRequestBuilders.get("/logout.do").session((MockHttpSession) session))
            .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
            .andExpect(MockMvcResultMatchers.redirectedUrl("/index"));

    assertNull(session.getAttribute("user"));
}


    @Test
    public void testQuit() throws Exception {
        // 有效类测试：管理员成功退出
        HttpSession session = mockMvc.perform(MockMvcRequestBuilders.get("/login"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("login"))
                .andReturn().getRequest().getSession();
        session.setAttribute("admin", new User());

        mockMvc.perform(MockMvcRequestBuilders.get("/quit.do").session((MockHttpSession) session))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/index"));

        assertNull(session.getAttribute("admin"));

    }

    @Test
    public void testUpdateUser() throws Exception {
        // 模拟从数据库中获取的用户对象
        User mockUser = new User();
        mockUser.setUserID("testUser");
        when(userService.findByUserID(anyString())).thenReturn(mockUser);

        // 模拟用户上传的图片
        MockMultipartFile picture = new MockMultipartFile("picture", "test.jpg", "image/jpeg", new byte[0]);

        // 发起更新用户信息的请求
        mockMvc.perform(MockMvcRequestBuilders.multipart("/updateUser.do")
                        .file(picture)
                        .param("userName", "Updated User")
                        .param("userID", "testUser")
                        .param("passwordNew", "newPassword")
                        .param("email", "updated@example.com")
                        .param("phone", "1234567890"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("user_info"));

    }

    @Test
    public void testUpdateUserInvalid() throws Exception {
        // 模拟从数据库中获取的用户对象
        User mockUser = new User();
        mockUser.setUserID("testUser");
        when(userService.findByUserID(anyString())).thenReturn(mockUser);

        // 模拟用户上传的图片（这次不提供图片，模拟没有上传图片）
        MockMultipartFile picture = new MockMultipartFile("picture", "", "image/jpeg", new byte[0]);

        // 发起更新用户信息的请求，但不提供用户名和密码，这会导致请求失败
        mockMvc.perform(MockMvcRequestBuilders.multipart("/updateUser.do")
                        .file(picture)
                        .param("userID", "testUser")
                        .param("email", "updated@example.com")
                        .param("phone", "1234567890"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

    }

    @Test
    public void testCheckPasswordValid() throws Exception {
        String userID = "testUser";
        String password = "testPassword";
        User mockUser = new User();
        mockUser.setUserID(userID);
        mockUser.setPassword(password);
        when(userService.findByUserID(userID)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/checkPassword.do")
                        .param("userID", userID)
                        .param("password", password))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
    }

    @Test
    public void testCheckPasswordInvalid() throws Exception {
        String userID = "testUser";
        String correctPassword = "correctPassword";
        String wrongPassword = "wrongPassword";
        User mockUser = new User();
        mockUser.setUserID(userID);
        mockUser.setPassword(correctPassword); // 设置正确的密码
        when(userService.findByUserID(userID)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/checkPassword.do")
                        .param("userID", userID)
                        .param("password", wrongPassword)) // 使用错误的密码
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("false"));
    }













    // 添加其他方法的测试，如register.do、logout.do、updateUser.do等
}
