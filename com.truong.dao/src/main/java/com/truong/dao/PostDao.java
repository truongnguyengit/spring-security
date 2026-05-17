package com.truong.dao;

import java.util.List;

import com.truong.entity.Post;

public interface PostDao {
	
	public Post create(Post post);
	
	public void update(Post post);
	
	public Post findOne(int id);
	
	public List<Post> findAll(int employeeId, int status) throws Exception;

	public Post spCreatePost(Post post) throws Exception;
}
