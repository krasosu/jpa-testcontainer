package com.example.jpatestcontainer.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public final class BlogDtos {
  private BlogDtos() {}

  public record CreateBlogRequest(@NotBlank @Size(max = 120) String name) {}

  public record BlogResponse(Long id, String name) {}

  public record CreatePostRequest(@NotBlank @Size(max = 200) String title, @NotBlank String body) {}

  public record PostResponse(Long id, Long blogId, String title, String body) {}

  public record CreateAuthorRequest(
      @NotBlank @Size(max = 120) String displayName, @NotBlank @Email @Size(max = 254) String email) {}

  public record AuthorResponse(Long id, Long blogId, String displayName, String email) {}

  public record CreateTagRequest(@NotBlank @Size(max = 80) String name) {}

  public record TagResponse(Long id, Long blogId, String name) {}

  public record LinkTagToPostRequest(Long tagId) {}

  public record CreateCommentRequest(
      Long postId, @NotBlank @Size(max = 120) String authorName, @NotBlank String text) {}

  public record CommentResponse(Long id, Long blogId, Long postId, String authorName, String text) {}

  public record BlogDetailsResponse(
      Long id,
      String name,
      List<PostResponse> posts,
      List<AuthorResponse> authors,
      List<TagResponse> tags,
      List<CommentResponse> comments) {}
}

