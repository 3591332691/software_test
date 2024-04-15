package com.demo.messageServiceTests;

import com.demo.controller.user.MessageController;
import com.demo.exception.LoginException;
import com.demo.service.MessageService;
import com.demo.service.MessageVoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.ui.Model;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @InjectMocks
    private MessageController messageController;

    @Mock
    private MessageService messageService;

    @Mock
    private MessageVoService messageVoService;

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

        assertThrows(LoginException.class, () -> {
            messageController.message_list(mock(Model.class), request);
        });

    }
}
