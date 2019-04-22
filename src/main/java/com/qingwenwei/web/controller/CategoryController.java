package com.qingwenwei.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.qingwenwei.constant.ResponseResult;
import com.qingwenwei.persistence.model.Category;
import com.qingwenwei.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.qingwenwei.service.PostService;

@Controller
public class CategoryController {

	private static Integer pageSize = 10;

	@Autowired
	private PostService postService;

	@Autowired
	private CategoryService categoryService;

	@RequestMapping(value = "/category/{categoryName}", method = RequestMethod.GET)
	public String getCategoryPostsByPage(@PathVariable String categoryName, Model model,
			@RequestParam(value = "p", required = false) Integer pageNum) {
		if (null == categoryName) {
			return "error/404";
		}
		int currPage = pageNum == null ? 1 : pageNum;
		Map<String, Object> attributes = this.postService.findPostsListByCategoryByPage(categoryName, currPage,
				pageSize);
		if (null == attributes) {
			return "error/404";
		}
		model.addAllAttributes(attributes);
		return "forum/home";
	}

	@GetMapping("/category/h5/{categoryID}")
	@ResponseBody
	public ResponseResult getCategoryPostsByPage(@PathVariable String categoryID,
												 @RequestParam(value = "page", required = false) Integer pageNum) {
		ResponseResult rs = new ResponseResult();

		String categoryName = categoryService.getCategoryNameByID(categoryID);

		if (null == categoryID || categoryName == null) {
			rs.setCode("404");
			rs.setMessage("目录不存在");
			rs.setData(null);
			return rs;
		}

		int currPage = pageNum == null ? 1 : pageNum;
		Map<String, Object> attributes = this.postService.findPostsListByCategoryByPage(categoryName, currPage,
				pageSize);

		List<Map> list = (List<Map>) attributes.get("pri_data");
		Map<String, Object> data = new HashMap<>();
		data.put("list", list);
		data.put("total", attributes.get("pri_total"));
		rs.setData(data);
		rs.setCode("200");
		rs.setMessage("正常调用");
		return rs;
	}

	@GetMapping("/category/h5/list")
	@ResponseBody
	public ResponseResult getCategoryList() {
		ResponseResult rs = new ResponseResult();

		List<Category> result = categoryService.findAll();

		Map<String, Object> data = new HashMap<>();
		data.put("list", result);
		data.put("total", result.size());
		rs.setData(data);
		rs.setCode("200");
		rs.setMessage("正常调用");
		return rs;
	}
}
