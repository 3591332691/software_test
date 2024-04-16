package com.demo;

import com.demo.controller.admin.AdminMessageController;
import com.demo.dao.MessageDao;
import com.demo.entity.Message;
import com.demo.entity.vo.MessageVo;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.*;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class AdminMessageServiceTest {
    @InjectMocks
    private AdminMessageController adminMessageController;
    @Mock
    private MessageService messageService;

    @Mock
    private MessageVoService messageVoService;
    @Mock
    private MessageDao messageDao;

    @Test
    public void testMessageManage() {
        // 模拟Model对象
        Model model = Mockito.mock(Model.class);
        // 模拟消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(new Message());
        messages.add(new Message());

        // 模拟Page对象
        Page<Message> messagePage = new PageImpl<>(messages);
        // 模拟调用findWaitState方法返回的Page对象
        Mockito.when(messageService.findWaitState(Mockito.any(Pageable.class))).thenReturn(messagePage);

        // 调用方法
        String viewName = adminMessageController.message_manage(model);

        // 验证视图名称是否正确返回
        Assertions.assertEquals("admin/message_manage", viewName);
    }
    @Test
    public void testMessageList0() {
        // 模拟消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(new Message());

        // 模拟消息VO列表
        List<MessageVo> messageVos = new ArrayList<>();
        messageVos.add(new MessageVo());

        // 模拟Page对象
        Page<Message> messagePage = new PageImpl<>(messages);

        // 模拟调用findWaitState方法返回的Page对象
        when(messageService.findWaitState(Mockito.any(Pageable.class))).thenReturn(messagePage);

        // 模拟调用returnVo方法返回的消息VO列表
        when(messageVoService.returnVo(Mockito.anyList())).thenReturn(messageVos);

        // 调用方法
        List<MessageVo> result = adminMessageController.messageList(1);

        // 验证返回的消息VO列表是否与预期相符
        Assertions.assertEquals(messageVos.size(), result.size());
    }
    @Test
    public void testMessageList1() {
        // 模拟消息列表
        List<Message> messages = new ArrayList<>();
        //messages.add(new Message());

        // 模拟消息VO列表
        List<MessageVo> messageVos = new ArrayList<>();
        //messageVos.add(new MessageVo());

        // 模拟Page对象
        Page<Message> messagePage = new PageImpl<>(messages);

        // 模拟调用findWaitState方法返回的Page对象
        when(messageService.findWaitState(Mockito.any(Pageable.class))).thenReturn(messagePage);

        // 模拟调用returnVo方法返回的消息VO列表
        when(messageVoService.returnVo(Mockito.anyList())).thenReturn(messageVos);

        // 调用方法
        List<MessageVo> result = adminMessageController.messageList(1);

        // 验证返回的消息VO列表是否与预期相符
        Assertions.assertEquals(messageVos.size(), result.size());
    }

    /**
     *通过一条待审核留言
     */
    @Test
    public void testPassMessage0() {
        // 设置要确认的消息的ID
        int messageId = 123;
        Message message = new Message();
        message.setMessageID(123);
        message.setState(1);
        // 模拟 messageService.confirmMessage 方法的行为
        Mockito.doNothing().when(messageService).confirmMessage(messageId);

        // 调用 passMessage 方法
        boolean result = adminMessageController.passMessage(messageId);

        // 验证结果是否符合预期
        assertTrue(result); // 假设确认通过返回 true
    }

    /**
     * 通过一条不通过留言,应该抛出异常
     */
    @Test
    public void testPassMessage1() {
        // 准备测试数据
        int messageId = 123;
        Message message = new Message();
        message.setMessageID(123);
        message.setState(3);
        // 模拟 messageService.confirmMessage 方法抛出异常
        //Mockito.doThrow(RuntimeException.class).when(messageService).confirmMessage(messageId);
        //TODO:这里没有调用真正的方法而是模拟了抛出异常的行为
        // 调用 passMessage 方法并捕获异常
        //Mockito.when(messageDao.findByMessageID(messageId)).thenReturn(message);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminMessageController.passMessage(messageId);
        });
    }
    /**
     * 通过一条不存在的留言
     */
    @Test
    public void testPassMessage2() {
        // 准备测试数据
        int messageId = 123;
        Message message = new Message();
        message.setMessageID(123);
        message.setState(3);
        // 模拟 messageService.confirmMessage 方法抛出异常
        Mockito.doThrow(new RuntimeException("留言不存在")).when(messageService).confirmMessage(messageId);

        //TODO:这里没有调用真正的方法而是模拟了抛出异常的行为
        // 调用 passMessage 方法并捕获异常
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminMessageController.passMessage(messageId);
        });

        // 验证异常消息是否符合预期
        assertEquals("留言不存在", exception.getMessage());
    }

    /**
     * 驳回一条待审核的留言
     */
    @Test
    public void testRejectMessage0() {
        // 准备测试数据
        int messageId = 123;

        // 调用 rejectMessage 方法
        boolean result = adminMessageController.rejectMessage(messageId);

        // 验证 messageService.rejectMessage 方法是否被调用
        Mockito.verify(messageService).rejectMessage(messageId);

        // 验证结果是否为 true
        assertTrue(result);
    }

    /**
     * 驳回一条不存在的留言
     */
    @Test
    public void testRejectMessage1() {
        // 准备测试数据
        int messageId = 123;


        // 验证 messageService.rejectMessage 方法是否被调用
        Mockito.doThrow(new RuntimeException("留言不存在")).when(messageService).rejectMessage(messageId);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminMessageController.rejectMessage(messageId);
        });

        // 验证异常消息是否符合预期
        assertEquals("留言不存在", exception.getMessage());
    }
    @Test
    public void testDelMessag0() {
        // 准备测试数据
        int messageId = 123;

        // 调用 delMessage 方法
        boolean result = adminMessageController.delMessage(messageId);

        // 验证 messageService.delById 方法是否被调用
        Mockito.verify(messageService).delById(messageId);

        // 验证结果是否为 true
        assertTrue(result);
    }

    /**
     *
     */
    @Test
    public void testDelMessag1() {
        // 准备测试数据
        int messageId = 123;

        // 验证 messageService.rejectMessage 方法是否被调用
        Mockito.doThrow(new RuntimeException("留言不存在")).when(messageService).rejectMessage(messageId);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            adminMessageController.rejectMessage(messageId);
        });

        // 验证异常消息是否符合预期
        assertEquals("留言不存在", exception.getMessage());
    }



}
