package com.demo;

import com.demo.controller.user.NewsController;
import com.demo.entity.News;
import com.demo.service.NewsService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

public class NewsControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private Model model;
    
    @Mock
    private NewsService newsService;

    @InjectMocks
    private NewsController newsController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);

        // 设置InternalResourceViewResolver，用于解析视图名称
        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
        viewResolver.setPrefix("/WEB-INF/views/");
        viewResolver.setSuffix(".jsp");

        // 初始化MockMvc
        mockMvc = MockMvcBuilders.standaloneSetup(newsController)
            .setViewResolvers(viewResolver)
            .build();
    }
    @Test
    public void testNews_ValidNewsID() throws Exception {
        // 准备测试数据
        int validNewsID = 1;
        News validNews = new News(validNewsID, "Test Title", "Test Content", LocalDateTime.now());
        when(newsService.findById(validNewsID)).thenReturn(validNews);
        
        // 发起请求并验证结果
        mockMvc.perform(get("/news")
                .param("newsID", String.valueOf(validNewsID)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news"))
                .andExpect(model().attribute("news", validNews))
                .andExpect(view().name("news"));
    }

    @Test
    public void testNews_InvalidNewsID() throws Exception {
        // 准备测试数据
        int invalidNewsID = -1; // 无效的newsID
        when(newsService.findById(invalidNewsID)).thenReturn(null); // 返回null表示未找到对应的新闻
        
        // 发起请求并验证结果
        assertThrows(Exception.class, () ->
            mockMvc.perform(get("/news")
                .param("newsID", String.valueOf(invalidNewsID))) 
        );

    }

    @Test
    public void testNewsList_ValidPage() throws Exception {
        // 准备测试数据
        int validPage = 2;
        Pageable pageable = PageRequest.of(validPage - 1, 5, Sort.by("time").descending());
        List<News> newsList = Arrays.asList(
            new News(1, "Title 1", "Content 1", LocalDateTime.now()),
            new News(2, "Title 2", "Content 2", LocalDateTime.now())
        );
        Page<News> newsPage = new PageImpl<>(newsList, pageable, newsList.size());
        when(newsService.findAll(pageable)).thenReturn(newsPage);
        
        // 发起请求并验证结果
        mockMvc.perform(get("/news_list")
                .param("page", String.valueOf(validPage)))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("newsPage"))
                .andExpect(model().attribute("newsPage", newsPage))
                .andExpect(view().name("news_list"));
        
    }

    @Test
    public void testNewsList_InvalidPage() throws Exception {
        // 准备测试数据
        int invalidPage = -1; // 无效的page值
        //Pageable pageable = PageRequest.of(invalidPage - 1, 5, Sort.by("time").descending());
        //when(newsService.findAll(pageable)).thenThrow(new IllegalArgumentException());
        
        assertThrows(Exception.class, () ->
            // 发起请求并验证结果
            mockMvc.perform(get("/news_list")
                .param("page", String.valueOf(invalidPage)))
                .andExpect(status().isBadRequest())
        );
    }

     @Test
    public void testNewsList_Total() throws Exception {
        // 准备测试数据
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        List<News> newsList = Arrays.asList(
            new News(1, "Title 1", "Content 1", LocalDateTime.now()),
            new News(2, "Title 2", "Content 2", LocalDateTime.now())
        );
        Page<News> newsPage = new PageImpl<>(newsList, pageable, newsList.size());
        when(newsService.findAll(pageable)).thenReturn(newsPage);
        
        // 发起请求并验证结果
        mockMvc.perform(get("/news_list"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("news_list"))
                .andExpect(model().attributeExists("total"))
                .andExpect(view().name("news_list"));
    }

    
}
