package com.demo;

import com.demo.controller.admin.AdminNewsController;
import com.demo.entity.News;
import com.demo.service.NewsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.http.MediaType;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



public class AdminNewsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private Model model;
    
    @Mock
    private NewsService newsService;

    @InjectMocks
    private AdminNewsController adminNewsController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // 设置InternalResourceViewResolver，用于解析视图名称
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(adminNewsController)
            .setViewResolvers(viewResolver)
            .build();
    }

    @Test
    public void testNewsManage() throws Exception {
        // 模拟newsService的返回数据
        List<News> newsList = new ArrayList<>();
        newsList.add(new News(1, "News 1", "Content 1", LocalDateTime.now()));

        Page<News> newsPage = new PageImpl<>(newsList);
        when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);

        // 发起GET请求
        mockMvc.perform(get("/news_manage"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/news_manage"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attribute("total", newsPage.getTotalPages()));

        //verify(model, times(1)).addAttribute(anyString(), any());
    }

    @Test
    public void testNewsEdit_ValidNewsID() throws Exception {
        int newsID = 1;
        News news = new News(newsID, "News 1", "Content 1", LocalDateTime.now());
        when(newsService.findById(newsID)).thenReturn(news);
        
        // 发起GET请求
        mockMvc.perform(get("/news_edit").param("newsID", String.valueOf(newsID)))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit"))
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", news));
        
    }

    @Test
    public void testNewsEdit_InvalidNewsID() throws Exception {
        int newsID = -1; // 无效newsID
        when(newsService.findById(newsID)).thenReturn(null); // 模拟返回空对象
        assertThrows(Exception.class, () ->
        // 发起 GET 请求
            mockMvc.perform(get("/news_edit").param("newsID", String.valueOf(newsID)))
                .andExpect(status().isOk())
                .andExpect(view().name("/admin/news_edit")) 
                .andExpect(model().attributeDoesNotExist("news"))
        );
    }

    @Test
    public void testNewsList() throws Exception {
        Page<News> newsPage = new PageImpl<>(Arrays.asList(new News(), new News()));
        when(newsService.findAll(any(Pageable.class))).thenReturn(newsPage);
        
        mockMvc.perform(get("/newsList.do").param("page", "2"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2)); 
    }
    

    @Test
    public void testNewsList_NegativePage() throws Exception {
        int negativePage = -1; // 负数的 page 值

        // 发起获取新闻列表的请求并传入负数的 page 值
        assertThrows(Exception.class, () ->
            mockMvc.perform(get("/newsList.do")
                .param("page", String.valueOf(negativePage)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty())
        );

        // 验证 findAll 方法不应被调用
        verify(newsService, never()).findAll(any(Pageable.class));

    }

    @Test
    public void testDelNews_ValidNewsID() throws Exception {
        int newsID = 1;
        News news = new News(newsID, "News 1", "Content 1", LocalDateTime.now());
        when(newsService.findById(newsID)).thenReturn(news);
        //doNothing().when(newsService).delById(newsID);
        mockMvc.perform(post("/delNews.do").param("newsID", String.valueOf(newsID)))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));

        verify(newsService, times(1)).delById(newsID);
    }
    
    @Test
    public void testDelNews_InvalidNewsID() throws Exception {
        int invalidNewsID = -1;
        // 模拟 findById 返回 null，表示无效的新闻ID
        when(newsService.findById(invalidNewsID)).thenReturn(null);

        assertThrows(Exception.class, () ->
            mockMvc.perform(post("/delNews.do").param("newsID", String.valueOf(invalidNewsID)))
        );
    
        verify(newsService, never()).delById(invalidNewsID);
    }

    @Test
    public void testModifyNews_ValidAll() throws Exception {
        int validNewsID = 1;
        String newTitle = "New Title";
        String newContent = "New Content";
        
        // 模拟 findById 返回值
        News validNews = new News(validNewsID, "Old Title", "Old Content", LocalDateTime.now());
        when(newsService.findById(validNewsID)).thenReturn(validNews);

        // 发起修改新闻的请求
        mockMvc.perform(post("/modifyNews.do")
                .param("newsID", String.valueOf(validNewsID))
                .param("title", newTitle)
                .param("content", newContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));

        // 验证 update 方法是否被调用
        verify(newsService, times(1)).update(validNews);
    }

    @Test
    public void testModifyNews_InvalidNewsID() throws Exception {
        int invalidNewsID = -1;
        String newTitle = "New Title";
        String newContent = "New Content";

        News nullNews = null;
        // 模拟 findById 返回 null，表示无效的新闻ID
        when(newsService.findById(invalidNewsID)).thenReturn(nullNews);
        //when(newsService.findById(invalidNewsID)).thenReturn(null);
        

        // 发起修改新闻的请求并捕获空指针异常
        assertThrows(Exception.class, () ->
                mockMvc.perform(post("/modifyNews.do")
                        .param("newsID", String.valueOf(invalidNewsID))
                        .param("title", newTitle)
                        .param("content", newContent))
        );
        
        // 验证 update 方法不应被调用
        verify(newsService, never()).update(any(News.class));
    }

    @Test
    public void testModifyNews_InvalidContent() throws Exception {
        // 准备测试数据
        int validNewsID = 1;
        String validTitle = "New Title"; // 无效的title
        String invalidContent = "";

        // 模拟 findById 返回值
        News validNews = new News(validNewsID, "Old Title", "Old Content", LocalDateTime.now());
        when(newsService.findById(validNewsID)).thenReturn(validNews);

        assertThrows(Exception.class, () ->
            mockMvc.perform(post("/modifyNews.do")
                .param("newsID", String.valueOf(validNewsID))
                .param("title", validTitle)
                .param("content", invalidContent))
        );

        // 验证 update 方法不应被调用
        verify(newsService, never()).update(any(News.class));
    }

    @Test
    public void testModifyNews_InvalidTitle() throws Exception {
        // 准备测试数据
        int validNewsID = 1;
        String invalidTitle = ""; // 无效的title
        String validContent = "New Content";

        // 模拟 findById 返回值
        News validNews = new News(validNewsID, "Old Title", "Old Content", LocalDateTime.now());
        when(newsService.findById(validNewsID)).thenReturn(validNews);

        assertThrows(Exception.class, () ->
            mockMvc.perform(post("/modifyNews.do")
                .param("newsID", String.valueOf(validNewsID))
                .param("title", invalidTitle)
                .param("content", validContent))
        );

        // 验证 update 方法不应被调用
        verify(newsService, never()).update(any(News.class));
    } 

    @Test
    public void testAddNews_ValidParameters() throws Exception {
        
        String validTitle = "Test Title";
        String validContent = "Test Content";

        // 发起添加新闻的请求
        mockMvc.perform(post("/addNews.do")
                .param("title", validTitle)
                .param("content", validContent))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("news_manage"));
        
        // 验证函数调用
        verify(newsService).create(any(News.class));
        
    }

    @Test
    public void testAddNews_InvalidTitle() throws Exception {
        String invalidTitle = "";
        String validContent = "Test Content";
        
        assertThrows(Exception.class, () ->
            mockMvc.perform(post("/addNews.do")
                .param("title", invalidTitle)
                .param("content", validContent))
        );
    
        //验证create方法不应该调用
        verify(newsService, never()).create(any(News.class));
    }

    @Test
    public void testAddNews_InvalidContent() throws Exception {
        String validTitle = "Test Title";
        String invalidContent = "";
        
        assertThrows(Exception.class, () ->
            mockMvc.perform(post("/addNews.do")
                .param("title", validTitle)
                .param("content", invalidContent))
        );
    
        //验证create方法不应该调用
        verify(newsService, never()).create(any(News.class));
    }
}