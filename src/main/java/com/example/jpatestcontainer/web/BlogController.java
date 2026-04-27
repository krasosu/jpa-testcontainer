package com.example.jpatestcontainer.web;

import static com.example.jpatestcontainer.web.dto.BlogDtos.*;

import com.example.jpatestcontainer.domain.Author;
import com.example.jpatestcontainer.domain.Blog;
import com.example.jpatestcontainer.domain.Comment;
import com.example.jpatestcontainer.domain.Post;
import com.example.jpatestcontainer.domain.PostTagLink;
import com.example.jpatestcontainer.domain.Tag;
import com.example.jpatestcontainer.repo.BlogRepository;
import com.example.jpatestcontainer.repo.PostRepository;
import com.example.jpatestcontainer.repo.PostTagLinkRepository;
import com.example.jpatestcontainer.repo.TagRepository;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/blogs")
public class BlogController {
  private final BlogRepository blogRepository;
  private final PostRepository postRepository;
  private final TagRepository tagRepository;
  private final PostTagLinkRepository postTagLinkRepository;

  public BlogController(
      BlogRepository blogRepository,
      PostRepository postRepository,
      TagRepository tagRepository,
      PostTagLinkRepository postTagLinkRepository) {
    this.blogRepository = blogRepository;
    this.postRepository = postRepository;
    this.tagRepository = tagRepository;
    this.postTagLinkRepository = postTagLinkRepository;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BlogResponse createBlog(@Valid @RequestBody CreateBlogRequest request) {
    Blog saved = blogRepository.save(Blog.builder().name(request.name()).build());
    return new BlogResponse(saved.getId(), saved.getName());
  }

  @GetMapping
  public List<BlogResponse> listBlogs() {
    return blogRepository.findAll().stream().map(b -> new BlogResponse(b.getId(), b.getName())).toList();
  }

  @GetMapping("/{blogId}")
  @Transactional(readOnly = true)
  public BlogDetailsResponse getBlog(@PathVariable Long blogId) {
    Blog blog =
        blogRepository
            .findById(blogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

    return new BlogDetailsResponse(
        blog.getId(),
        blog.getName(),
        blog.getPosts().stream()
            .map(p -> new PostResponse(p.getId(), blog.getId(), p.getTitle(), p.getBody()))
            .toList(),
        blog.getAuthors().stream()
            .map(a -> new AuthorResponse(a.getId(), blog.getId(), a.getDisplayName(), a.getEmail()))
            .toList(),
        blog.getTags().stream().map(t -> new TagResponse(t.getId(), blog.getId(), t.getName())).toList(),
        blog.getComments().stream()
            .map(
                c ->
                    new CommentResponse(
                        c.getId(),
                        blog.getId(),
                        c.getPost() == null ? null : c.getPost().getId(),
                        c.getAuthorName(),
                        c.getText()))
            .toList());
  }

  /**
   * Example endpoint: returns the JPA entity directly.
   *
   * <p>To avoid LazyInitializationException with open-in-view disabled, the repository method uses
   * an EntityGraph to fetch all required children in the same transaction.
   */
  @GetMapping("/{blogId}/entity")
  @Transactional(readOnly = true)
  public Blog getBlogEntity(@PathVariable Long blogId) {
    return blogRepository
        .findWithChildrenById(blogId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
  }

  @PostMapping("/{blogId}/posts")
  @ResponseStatus(HttpStatus.CREATED)
  public PostResponse addPost(@PathVariable Long blogId, @Valid @RequestBody CreatePostRequest request) {
    Blog blog =
        blogRepository
            .findById(blogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));
    Post saved =
        postRepository.save(
            Post.builder().blog(blog).title(request.title()).body(request.body()).build());
    return new PostResponse(saved.getId(), blogId, saved.getTitle(), saved.getBody());
  }

  @PostMapping("/{blogId}/authors")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public AuthorResponse addAuthor(@PathVariable Long blogId, @Valid @RequestBody CreateAuthorRequest request) {
    Blog blog =
        blogRepository
            .findById(blogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

    Author author =
        Author.builder().blog(blog).displayName(request.displayName()).email(request.email()).build();
    blog.getAuthors().add(author);
    blogRepository.saveAndFlush(blog);

    return new AuthorResponse(author.getId(), blogId, author.getDisplayName(), author.getEmail());
  }

  @PostMapping("/{blogId}/tags")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public TagResponse addTag(@PathVariable Long blogId, @Valid @RequestBody CreateTagRequest request) {
    Blog blog =
        blogRepository
            .findById(blogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

    Tag tag = Tag.builder().blog(blog).name(request.name()).build();
    blog.getTags().add(tag);
    blogRepository.saveAndFlush(blog);

    return new TagResponse(tag.getId(), blogId, tag.getName());
  }

  @PostMapping("/{blogId}/comments")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public CommentResponse addComment(
      @PathVariable Long blogId, @Valid @RequestBody CreateCommentRequest request) {
    Blog blog =
        blogRepository
            .findById(blogId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Blog not found"));

    Post post = null;
    if (request.postId() != null) {
      post =
          postRepository
              .findById(request.postId())
              .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
      if (!post.getBlog().getId().equals(blogId)) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not part of this blog");
      }
    }

    Comment comment =
        Comment.builder()
            .blog(blog)
            .post(post)
            .authorName(request.authorName())
            .text(request.text())
            .build();
    blog.getComments().add(comment);
    blogRepository.saveAndFlush(blog);

    return new CommentResponse(
        comment.getId(), blogId, post == null ? null : post.getId(), comment.getAuthorName(), comment.getText());
  }

  @PostMapping("/{blogId}/posts/{postId}/tags")
  @ResponseStatus(HttpStatus.CREATED)
  @Transactional
  public void linkTagToPost(
      @PathVariable Long blogId, @PathVariable Long postId, @Valid @RequestBody LinkTagToPostRequest request) {
    Post post =
        postRepository
            .findById(postId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    if (!post.getBlog().getId().equals(blogId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post is not part of this blog");
    }

    Tag tag =
        tagRepository
            .findById(request.tagId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
    if (!tag.getBlog().getId().equals(blogId)) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tag is not part of this blog");
    }

    postTagLinkRepository.save(PostTagLink.builder().post(post).tag(tag).build());
  }
}

