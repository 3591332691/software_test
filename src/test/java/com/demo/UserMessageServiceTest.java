package com.demo;

import com.demo.controller.user.MessageController;
import com.demo.entity.Message;
import com.demo.entity.User;
import com.demo.entity.vo.MessageVo;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


public class UserMessageServiceTest {
    @Spy
    private MessageService messageService;

    @Mock(lenient = true)
    private MessageVoService messageVoService;
    //
    // @Autowired
    @InjectMocks
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
        List<Message> messageList = List.of();
        Page<Message> messages  = new PageImpl<>(messageList);
        Pageable message_pageable= PageRequest.of(0,5, Sort.by("time").descending());
        List<MessageVo> message_list = List.of();
        when(messageService.findPassState(message_pageable)).thenReturn(messages);
        when(messageVoService.returnVo(messages.getContent())).thenReturn(message_list);
        Pageable user_message_pageable = PageRequest.of(0,5, Sort.by("time").descending());
        when(messageService.findByUser("user1",user_message_pageable)).thenReturn(messages);
        //它验证了当用户已登录时，调用 messageController.message_list() 方法会正常运行
        Assertions.assertEquals("message_list", messageController.message_list(mock(Model.class),request));
    }

    /**
     * 以非登录状态查看“我的留言”
     * @throws Exception
     */
    @Test
    public void testGetMessage2() throws Exception {

        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(null); // 模拟 getSession().getAttribute() 返回 null

        assertThrows(LoginException.class, () -> {
            messageController.user_message_list(1,request);
        });
    }

    /**
     * 以登录状态查看“我的留言”
     * @throws Exception
     */
    @Test
    public void testGetMessage3() throws Exception {
        MockitoAnnotations.initMocks(this);
        //模拟用户user
        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        // 模拟消息列表
        List<Message> messages = new ArrayList<>();
        // 添加一些消息到列表中
        messages.add(new Message());
        messages.add(new Message());
        // 使用Mockito模拟消息服务的行为
        Page<Message> messagePage = new PageImpl<>(messages);
        Pageable message_pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        when(messageService.findByUser(user.getUserID(),message_pageable)).thenReturn(messagePage);
        // 模拟消息VO
        List<MessageVo> messageVos = new ArrayList<>();
        // 添加一些消息VO到列表中
        messageVos.add(new MessageVo());
        messageVos.add(new MessageVo());
        // 使用Mockito模拟消息VO服务的行为
        when(messageVoService.returnVo(any())).thenReturn(messageVos);


        // 模拟HttpServletRequest
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);


        // 调用控制器方法
        List<MessageVo> result = messageController.user_message_list(1, request);
        //verify(messageService).findByUser(user.getUserID(), (Pageable) messagePage);
        // 验证返回结果是否正确
        Assertions.assertEquals(2, result.size()); // 假设消息VO列表中有两条消息
    }

    /**
     * 以非登录状态查看“所有留言”
     * @throws Exception
     */
    @Test
    public void testGetMessage4() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);

        // 模拟HttpServletRequest对象

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(mock(HttpSession.class)); // 模拟 getSession() 返回 HttpSession 对象
        when(request.getSession().getAttribute("user")).thenReturn(null); // 模拟 getSession().getAttribute() 返回 null

        //它验证了当用户未登录时，调用 messageController.message_list() 方法是否会抛出 LoginException 异常。
        assertThrows(LoginException.class, () -> {
            messageController.message_list(1);
        });

    }

    /**
     * 以登录状态查看“所有留言”
     * @throws Exception
     */
    @Test
    public void testGetMessage5() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        int page = 1;
        Pageable message_pageable= PageRequest.of(page-1,5, Sort.by("time").descending());
        List<Message> messages = new ArrayList<>();
        // 添加一些消息到列表中
        messages.add(new Message());
        messages.add(new Message());
        List<MessageVo> messageVos = new ArrayList<>();
        // 添加一些消息VO到列表中
        messageVos.add(new MessageVo());
        messageVos.add(new MessageVo());
        // 使用Mockito模拟消息服务的行为
        Page<Message> messagePage = new PageImpl<>(messages);
        when(messageService.findPassState(message_pageable)).thenReturn( messagePage);
        when(messageVoService.returnVo( messages)).thenReturn( messageVos);
        //
        Assertions.assertEquals(2, messageController.message_list(page).size());
    }

    /**
     * 留言内容为空，发布留言
     * @throws Exception
     */
    @Test
    public void testPublishMessage0() throws Exception {
        // 创建一个模拟的 HttpServletResponse 对象
        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        //应该被拒绝，不创建message
        assertThrows(Exception.class, () -> {
            messageController.sendMessage("user1","",response);
        });
    }

    /**
     * 留言内容不为空，发布留言
     * @throws Exception
     */
    @Test
    public void testPublishMessage1() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        //想要再检测是否创建成功，已知user1是测试创造的不在库里，
        // 模拟HttpServletRequest
        //模拟用户user
        User user = new User();
        user.setUserID("user1");
        user.setUserName("username");;
        user.setPassword("password");
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
        when(session.getAttribute("user")).thenReturn(user);
        // 创建一个模拟的 HttpServletResponse 对象
        //发送了一个请求
        HttpServletResponse response = mock(HttpServletResponse.class);
        messageController.sendMessage("user1","message",response);
        //通过。。验证是否运行了sendMessage但是没有检测功能这里只检测了这个函数有没有运行到底
        verify(response).sendRedirect("/message_list");
    }

//    /**
//     * 留言内容不为空，发布留言
//     * @throws Exception
//     */
//    @Test
//    public void testPublishMessage1() throws Exception {
//        // 初始化mock对象
//        MockitoAnnotations.initMocks(this);
//        //想要再检测是否创建成功，已知user1是测试创造的不在库里，
//        // 模拟HttpServletRequest
//        //模拟用户user
//        User user = new User();
//        user.setUserID("user1");
//        user.setUserName("username");;
//        user.setPassword("password");
//        HttpServletRequest request = mock(HttpServletRequest.class);
//        HttpSession session = mock(HttpSession.class);
//        when(request.getSession()).thenReturn(session);
//        when(session.getAttribute("user")).thenReturn(user);
//        //模拟消息服务
//        int page = 1;
//        Pageable message_pageable= PageRequest.of(page-1,5, Sort.by("time").descending());
//        List<Message> messages = new ArrayList<>();
//        // 添加一些消息到列表中
//        messages.add(new Message());
//        messages.add(new Message());
//        List<MessageVo> messageVos = new ArrayList<>();
//        // 添加一些消息VO到列表中
//        messageVos.add(new MessageVo());
//        messageVos.add(new MessageVo());
//        // 使用Mockito模拟消息服务的行为
//        Page<Message> messagePage = new PageImpl<>(messages);
//        Mockito.when(messageVoService.returnVo( messages)).thenReturn( messageVos);
//        Mockito.when(messageService.findByUser("user1",message_pageable)).thenReturn(messagePage);
//        // 创建一个模拟的 HttpServletResponse 对象
//        //发送了一个请求
//        HttpServletResponse response = mock(HttpServletResponse.class);
//        messageController.sendMessage("user1","message",response);
//        // 初始化mock对象
//        MockitoAnnotations.initMocks(this);
//        Message message = new Message();
//        message.setContent("message");
//        message.setUserID("user1");
//        //通过。。验证是否运行了sendMessage但是没有检测功能这里只检测了这个函数有没有运行到底
//        verify(response).sendRedirect("/message_list");
//        Assertions.assertEquals(messageVos.size()+1, messageController.user_message_list(1,request).size());
//    }

    /**
     * 修改 内容不为空
     */
    @Test
    public void testModifyMessage0() throws Exception {

        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        int messageId = 2;
        Message message = new Message();
        message.setMessageID(messageId);
        message.setContent("123");
        when(messageService.findById(messageId)).thenReturn(message);
        Assertions.assertEquals(true, messageController.modifyMessage(messageId,"modified content",response));

    }

    /**
     * 修改 内容为空,应该抛出异常
     */
    @Test
    public void testModifyMessage1() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        //发送了一个空内容的请求
        MockHttpServletResponse response = new MockHttpServletResponse();
        int messageId = 2;
        Message message = new Message();
        message.setMessageID(messageId);
        message.setContent("123");
        Mockito.when(messageService.findById(messageId)).thenReturn(message);
        assertThrows(Exception.class, () -> {
            messageController.modifyMessage(messageId,"",response);
        });
    }

    /**
     * 修改 一条不存在的留言
     */
    @Test
    public void testModifyMessage2() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        Message message = new Message();
        message.setContent("modified content");
        message.setUserID("user1");
        message.setMessageID(0);

        // 模拟HttpServletRequest对象
        HttpServletResponse response = mock(HttpServletResponse.class);
        //modifyMessage(int messageID,String content, HttpServletResponse response)
        when(messageService.findById(0)).thenReturn(null);
        assertThrows(Exception.class, () -> {
            messageController.modifyMessage(0,"modified",response);
        });
    }

    /**
     * 删除一条待审核的留言
     * @throws Exception
     */
    @Test
    public void testDeleteMessage0() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        int MessageId = 2;
        Message message = new Message();
        message.setContent("123");
        message.setMessageID(MessageId);
        message.setState(1);
        Mockito.when(messageService.findById(MessageId)).thenReturn(message);
        Assertions.assertTrue(messageController.delMessage(MessageId));
    }

    /**
     * 删除一条不存在的留言
     * @throws Exception
     */
    @Test
    public void testDeleteMessage1() throws Exception {
        // 初始化mock对象
        MockitoAnnotations.initMocks(this);
        int MessageId = 2;
        Message message = new Message();
        message.setContent("modified content");
        message.setUserID("user1");
        message.setMessageID(0);
        Mockito.when(messageService.findById(MessageId)).thenReturn(null);
        assertThrows(Exception.class, () -> {
            messageController.delMessage(MessageId);
        });
    }


}
