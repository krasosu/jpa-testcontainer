package com.example.jpatestcontainer.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jpatestcontainer.testsupport.AbstractControllerIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class BlogControllerTest extends AbstractControllerIT {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;

  @Test
  void createBlog_addPost_andFetchDetails() throws Exception {
    MvcResult createdBlog =
        mockMvc
            .perform(
                post("/api/blogs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name":"My Blog"}
                        """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("My Blog"))
            .andReturn();

    JsonNode blogJson = objectMapper.readTree(createdBlog.getResponse().getContentAsString());
    long blogId = blogJson.get("id").asLong();

    mockMvc
        .perform(
            post("/api/blogs/{blogId}/posts", blogId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {"title":"Hello","body":"World"}
                        """))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").isNumber())
        .andExpect(jsonPath("$.blogId").value((int) blogId))
        .andExpect(jsonPath("$.title").value("Hello"));

    mockMvc
        .perform(get("/api/blogs/{blogId}", blogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value((int) blogId))
        .andExpect(jsonPath("$.posts.length()").value(1))
        .andExpect(jsonPath("$.posts[0].title").value("Hello"));
  }

  @Test
  void getBlogEntity_usesEntityGraphSoLazyCollectionsAreInitialized() throws Exception {
    MvcResult createdBlog =
        mockMvc
            .perform(
                post("/api/blogs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"name":"Entity Blog"}
                        """))
            .andExpect(status().isCreated())
            .andReturn();

    long blogId = objectMapper.readTree(createdBlog.getResponse().getContentAsString()).get("id").asLong();

    mockMvc
        .perform(
            post("/api/blogs/{blogId}/posts", blogId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {"title":"P1","body":"B1"}
                        """))
        .andExpect(status().isCreated());

    mockMvc
        .perform(get("/api/blogs/{blogId}/entity", blogId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value((int) blogId))
        .andExpect(jsonPath("$.posts.length()").value(1));
  }
}

