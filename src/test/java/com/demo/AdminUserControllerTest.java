package com.demo;

import com.demo.controller.admin.AdminUserController;
import com.demo.entity.User;
import com.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminUserController.class)
public class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    public void testUserManage() throws Exception {
        Page<User> mockUsers = new PageImpl<>(Collections.emptyList());
        when(userService.findByUserID(any(PageRequest.class))).thenReturn(mockUsers);

        mockMvc.perform(MockMvcRequestBuilders.get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user_manage"));
    }

    @Test
    public void testUserManageNoUsers() throws Exception {
        when(userService.findByUserID(any(PageRequest.class))).thenReturn(null);

        mockMvc.perform(MockMvcRequestBuilders.get("/user_manage"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user_manage"));
    }

    @Test
    public void testUserAdd() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/user_add"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.view().name("admin/user_add"));
    }

    @Test
    public void testUserList() throws Exception {
        when(userService.findByUserID(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(MockMvcRequestBuilders.get("/userList.do"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("[]"));
    }

    @Test
    public void testUserEdit() throws Exception {
        int id = 1;
        User mockUser = new User();
        when(userService.findById(id)).thenReturn(mockUser);

        mockMvc.perform(MockMvcRequestBuilders.get("/user_edit?id=" + id))
                .andExpect(status().isOk())
                .andExpect(model().attribute("user", mockUser))
                .andExpect(MockMvcResultMatchers.view().name("admin/user_edit"));
    }

    @Test
    public void testModifyUserValid() throws Exception {
        // 构造请求参数
        String userID = "newUserID";
        String oldUserID = "oldUserID";
        String userName = "New User";
        String password = "newPassword";
        String email = "new@example.com";
        String phone = "9876543210";

        // 构造期望的用户对象
        User user = new User();
        user.setUserID(oldUserID);
        user.setUserName("Old User");
        user.setPassword("oldPassword");
        user.setEmail("old@example.com");
        user.setPhone("1234567890");

        // 设置期望的userService.findByUserID返回值
        when(userService.findByUserID(oldUserID)).thenReturn(user);

        mockMvc.perform(post("/modifyUser.do")
                        .param("userID", userID)
                        .param("oldUserID", oldUserID)
                        .param("userName", userName)
                        .param("password", password)
                        .param("email", email)
                        .param("phone", phone))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("user_manage"));

        // 验证userService.updateUser是否被调用，并传入了修改后的用户对象

    }



    @Test
    public void testAddUser() throws Exception {
        mockMvc.perform(post("/addUser.do")
                        .param("userID", "testUser")
                        .param("userName", "Test User")
                        .param("password", "testPassword")
                        .param("email", "test@example.com")
                        .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("user_manage"));
    }

    @Test
    public void testCheckUserIDValid() throws Exception {
        String userID = "newUserID";

        // 模拟数据库中不存在相同的userID
        when(userService.countUserID(userID)).thenReturn(0);

        mockMvc.perform(post("/checkUserID.do").param("userID", userID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("true"));
    }

    @Test
    public void testCheckUserIDInvalid() throws Exception {
        String existingUserID = "existingUserID";

        // 模拟数据库中已存在相同的userID
        when(userService.countUserID(existingUserID)).thenReturn(1);

        mockMvc.perform(post("/checkUserID.do").param("userID", existingUserID))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("false"));
    }
}
