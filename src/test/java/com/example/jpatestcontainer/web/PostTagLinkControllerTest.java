package com.example.jpatestcontainer.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.jpatestcontainer.domain.PostTagLink;
import com.example.jpatestcontainer.repo.PostTagLinkRepository;
import com.example.jpatestcontainer.testsupport.AbstractControllerIT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class PostTagLinkControllerTest extends AbstractControllerIT {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired PostTagLinkRepository postTagLinkRepository;

  @Test
  void linkTagToPost_createsJoinRowWithoutUniqueConstraint() throws Exception {
    long blogId = createBlog("JoinTable Blog");

    long postId =
        createPost(blogId, "Post A", "Body A");

    long tagId =
        createTag(blogId, "Tag-1");

    mockMvc
        .perform(
            post("/api/blogs/{blogId}/posts/{postId}/tags", blogId, postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {"tagId": %d}
                        """
                        .formatted(tagId)))
        .andExpect(status().isCreated());

    assertThat(postTagLinkRepository.count()).isEqualTo(1);
    PostTagLink link = postTagLinkRepository.findAll().getFirst();
    assertThat(link.getPost().getId()).isEqualTo(postId);
    assertThat(link.getTag().getId()).isEqualTo(tagId);

    // If legacy/prod code creates a duplicate link, we explicitly avoid UNIQUE(post_id, tag_id).
    mockMvc
        .perform(
            post("/api/blogs/{blogId}/posts/{postId}/tags", blogId, postId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                        {"tagId": %d}
                        """
                        .formatted(tagId)))
        .andExpect(status().isCreated());

    assertThat(postTagLinkRepository.count()).isEqualTo(2);
  }

  private long createBlog(String name) throws Exception {
    MvcResult createdBlog =
        mockMvc
            .perform(
                post("/api/blogs")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {"name": "%s"}
                            """
                            .formatted(name)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn();
    JsonNode blogJson = objectMapper.readTree(createdBlog.getResponse().getContentAsString());
    return blogJson.get("id").asLong();
  }

  private long createPost(long blogId, String title, String body) throws Exception {
    MvcResult createdPost =
        mockMvc
            .perform(
                post("/api/blogs/{blogId}/posts", blogId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {"title": "%s", "body": "%s"}
                            """
                            .formatted(title, body)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn();
    JsonNode postJson = objectMapper.readTree(createdPost.getResponse().getContentAsString());
    return postJson.get("id").asLong();
  }

  private long createTag(long blogId, String name) throws Exception {
    MvcResult createdTag =
        mockMvc
            .perform(
                post("/api/blogs/{blogId}/tags", blogId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {"name": "%s"}
                            """
                            .formatted(name)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andReturn();
    JsonNode tagJson = objectMapper.readTree(createdTag.getResponse().getContentAsString());
    return tagJson.get("id").asLong();
  }
}

