package com.demo.messageServiceTests;

import com.demo.controller.user.MessageController;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Autowired
    private MessageController messageController;

    /**
     * 测试以未登录状态想要看留言板
     */
    @Test
    public void testGetMessage0() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(null); // 模拟 getSession().getAttribute() 返回 null

        //它验证了当用户未登录时，调用 messageController.message_list() 方法是否会抛出 LoginException 异常。
        assertThrows(LoginException.class, () -> {
            messageController.message_list(mock(Model.class), request);
        });

    }

    /**
     * 以登录状态查看留言板,测试是否页面跳转成功
     * @throws Exception
     */
    @Test
    public void testGetMessage1() throws Exception {
        //这里初始化数据库，设置有一个叫做user1的用户
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testGetMessage1.sql");
        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null

        //它验证了当用户已登录时，调用 messageController.message_list() 方法会正常运行
        assertEquals("message_list",messageController.message_list(mock(Model.class),request));
    }

    /**
     * 以登录状态查看“我的留言”（此时我的留言有0条）
     * @throws Exception
     */
    @Test
    public void testGetMessage2() throws Exception {
        //这里初始化数据库，设置有一个叫做user1的用户并且库里没有user1的留言
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testGetMessage2.sql");
        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null

        //messageController.user_message_list(1,request);
        assertEquals(0,messageController.user_message_list(1,request).size());
    }

    /**
     * 以登录状态查看“我的留言”（此时我的留言有1条）
     * @throws Exception
     */
    @Test
    public void testGetMessage3() throws Exception {
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testGetMessage3.sql");
        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null

        //messageController.user_message_list(1,request);
        assertEquals(1,messageController.user_message_list(1,request).size());
    }

    /**
     * 以登录状态查看“所有留言”（此时所有留言有0条）
     * @throws Exception
     */
    @Test
    public void testGetMessage4() throws Exception {
        //把message清空了
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testGetMessage4.sql");

        //以登录状态查看“所有留言”（此时所有留言有0条）
        assertEquals(0,messageController.message_list(1).size());
    }

    /**
     * 以登录状态查看“所有留言”（此时所有留言有1条）
     * @throws Exception
     */
    @Test
    public void testGetMessage5() throws Exception {
        //把message清空了,加一条留言
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testGetMessage5.sql");

        //以登录状态查看“所有留言”（此时所有留言有0条）
        assertEquals(1,messageController.message_list(1).size());
    }

    /**
     * 留言内容为空，发布留言
     * @throws Exception
     */
    @Test
    public void testPublishMessage0() throws Exception {
        //把message清空了
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testPublishMessage0.sql");
        // 创建一个模拟的 HttpServletResponse 对象
        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        messageController.sendMessage("user1","",response);


        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null

        //应该被拒绝，不创建message
        Assertions.assertEquals(0, messageController.user_message_list(1,request).size());
    }
    /**
     * 留言内容不为空，发布留言
     * @throws Exception
     */
    @Test
    public void testPublishMessage1() throws Exception {
        //把message清空了
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testPublishMessage1.sql");
        // 创建一个模拟的 HttpServletResponse 对象
        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        messageController.sendMessage("user1","message",response);


        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null
        List<MessageVo> messageList  = messageController.user_message_list(1,request);
        MessageVo firstMessage = messageList.get(0);
        //状态应该是待审核（状态为1）
        Assertions.assertEquals(1, messageList.size());
        Assertions.assertEquals(1,firstMessage.getState());
    }

    /**
     * 修改 状态为待审核1的留言,状态应该为1
     */
    @Test
    public void testModifyMessage0() throws Exception {
        //把message清空了,加了一条待审核的留言（状态为1）
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testModifyMessage0.sql");

        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        messageController.modifyMessage(1,"modified content",response);

        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null
        List<MessageVo> messageList  = messageController.user_message_list(1,request);
        MessageVo firstMessage = messageList.get(0);
        //状态应该是待审核（状态为1）
        Assertions.assertEquals(1,firstMessage.getState());
        Assertions.assertEquals(1,messageList.size());
    }

    /**
     * 修改 状态为已通过2的留言, 状态应该为1
     */
    @Test
    public void testModifyMessage1() throws Exception {
        //把message清空了,加了一条已通过的留言（状态为2）
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testModifyMessage1.sql");

        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        messageController.modifyMessage(1,"modified content",response);

        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null
        List<MessageVo> messageList  = messageController.user_message_list(1,request);
        MessageVo firstMessage = messageList.get(0);
        //状态应该是待审核（状态为1）
        Assertions.assertEquals(1,firstMessage.getState());
        Assertions.assertEquals(1,messageList.size());
    }

    /**
     * 修改 状态为未通过3的留言, 状态应该为1
     */
    @Test
    public void testModifyMessage2() throws Exception {
        //把message清空了,加了一条未通过的留言（状态为3）
        TestDatabaseInitialization temp = new TestDatabaseInitialization();
        temp.initializeDatabase("src/test/resources/testModifyMessage2.sql");

        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        messageController.modifyMessage(1,"modified content",response);

        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(user); // 模拟 getSession().getAttribute() 返回 null
        List<MessageVo> messageList  = messageController.user_message_list(1,request);
        MessageVo firstMessage = messageList.get(0);
        //状态应该是待审核（状态为1）
        Assertions.assertEquals(1,firstMessage.getState());
        Assertions.assertEquals(1,messageList.size());
    }

}
