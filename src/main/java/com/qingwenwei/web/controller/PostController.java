package com.qingwenwei.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import com.qingwenwei.constant.ResponseResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import com.qingwenwei.exception.BadRequestException;
import com.qingwenwei.exception.ResourceNotFoundException;
import com.qingwenwei.persistence.model.Comment;
import com.qingwenwei.persistence.model.Post;
import com.qingwenwei.service.CategoryService;
import com.qingwenwei.service.CommentService;
import com.qingwenwei.service.PostService;
import com.qingwenwei.util.NewPostFormValidator;
import com.qingwenwei.web.dto.CommentDto;
import com.qingwenwei.web.dto.PostDto;

@Controller
public class PostController {

	private static final Logger logger = LoggerFactory.getLogger(PostController.class);

	@Autowired
	private PostService postService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CommentService commentService;

	@Autowired
	private NewPostFormValidator newPostValidator;

	@Autowired
	ApplicationEventPublisher eventPublisher;

	@RequestMapping(value = "/post/list", method = RequestMethod.GET)
	public String index(Model model) {
		Map<String, Object> attributes = postService.findPosts(1L);
		if (null == attributes) {
			throw new ResourceNotFoundException("attributes not found.");
		}
		model.addAttribute(attributes);
		return "fragments/posts-list";
	}

	@RequestMapping(value = "/post/{postId}", method = RequestMethod.GET)
	public String getPost(Model model, @PathVariable Long postId) {
		if (null == postId) {
			throw new BadRequestException("Path variable postId cound not be null.");
		}
		Map<String, Object> attributes = this.postService.findPostDetailsAndCommentsByPostId(postId);
		if (null == attributes) {
			throw new ResourceNotFoundException("attributes not found.");
		}
		model.addAllAttributes(attributes);
		return "forum/post";
	}

	@GetMapping("/post/h5/{postId}")
	@ResponseBody
	public ResponseResult getPostById(@PathVariable Long postId) {
		ResponseResult rs = new ResponseResult();
		try {
			if (null == postId) {
				throw new BadRequestException("Path variable postId cound not be null.");
			}
			Map<String, Object> attributes = this.postService.findPostDetailsAndCommentsByPostId(postId);
			if (null == attributes) {
				rs.setCode("404");
				rs.setMessage("未找到资源");
				return rs;
			}
			Map<String, Object> data = (Map<String, Object>)attributes.get("pri_data");
			rs.setData(data);
			rs.setCode("200");
			rs.setMessage("正常调用");
		} catch (Exception ex) {
			rs.setCode("500");
			rs.setMessage(ex.getMessage());
		}
		return rs;
	}

	@GetMapping("/post/h5/list")
	@ResponseBody
	public ResponseResult getPostList(@RequestParam(value = "page", required = false) Long pageNum) {
		ResponseResult rs = new ResponseResult();

		Long page = pageNum == null ? 1L : pageNum;

		Map<String, Object> attributes = postService.findPosts(page);
		if (null == attributes) {
			throw new ResourceNotFoundException("attributes not found.");
		}
		List<Map> list = (List<Map>) attributes.get("pri_data");
		Map<String, Object> data = new HashMap<>();
		data.put("list", list);
		data.put("total", attributes.get("pri_total"));
		rs.setData(data);
		rs.setCode("200");
		rs.setMessage("正常调用");
		return rs;
	}

	@RequestMapping(value = "/new/{categoryName}", method = RequestMethod.GET)
	public String displayNewPostPageWithCategory(Model model, @PathVariable String categoryName) {
		if (null == categoryName) {
			throw new BadRequestException("Path variable postId cound not be null.");
		}
		Map<String, Object> attributes = this.categoryService.getNewPostPageWithCategoryName(categoryName);
		if (null == attributes) {
			throw new ResourceNotFoundException("attributes not found.");
		}
		model.addAllAttributes(attributes);
		return "forum/new-post";
	}

	@RequestMapping(value = "/new", method = RequestMethod.GET)
	public String displayNewPostPage(Model model) {
		Map<String, Object> attributes = this.categoryService.getNewPostPageWithCategorySelect();
		if (null == attributes) {
			throw new ResourceNotFoundException("attributes not found.");
		}
		model.addAllAttributes(attributes);
		return "forum/new-post";
	}

	@RequestMapping(value = "/new", method = RequestMethod.POST)
	public String processNewPost(@Valid @ModelAttribute("postDto") PostDto postDto, BindingResult bindingResult,
			Model model) {
		if (null == postDto) {
			throw new BadRequestException("NewPostForm cound not be null.");
		}
		Post post = this.postService.createNewPost(postDto);
		if (null == post) {
			throw new ResourceNotFoundException("New post object can't be created.");
		}
		// post form validation
		this.newPostValidator.validate(post, bindingResult);
		if (bindingResult.hasErrors()) {
			logger.info("BindingResult has errors >> " + bindingResult.getFieldError());
			return "forum/new-post";
		} else {
			this.postService.save(post);
		}
		return "redirect:/";
	}

	@RequestMapping(value = "/post/{postId}", method = RequestMethod.POST)
	public String processNewComment(@PathVariable Long postId,
			@Valid @ModelAttribute("commentDto") CommentDto commentDto) {
		if (null == postId && null == commentDto) {
			throw new BadRequestException("Path variable postId and newCommentForm cound not be null.");
		}
		Comment comment = this.commentService.createNewCommentOnPost(postId, commentDto);
		if (null == comment) {
			throw new ResourceNotFoundException("New comment object can't be created.");
		}
		// comment form validation here ...
		this.commentService.save(comment);
		return "redirect:/post/{postId}";
	}

}