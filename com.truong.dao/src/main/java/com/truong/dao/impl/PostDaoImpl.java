package com.truong.dao.impl;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.truong.common.exception.CustomException;
import com.truong.dao.AbstractDao;
import com.truong.dao.PostDao;
import com.truong.entity.Post;
import com.truong.entity.custom.StoreProcedureStatusCodeEnum;
import com.truong.entity.Post;

@Repository("postDao")
@Transactional
public class PostDaoImpl extends AbstractDao<Integer, Post> implements PostDao{

	@Override
	public Post create(Post post) {
		 this.getSession().save(post);
		 return post;
	}

	@Override
	public void update(Post post) {
		this.getSession().update(post);
		
	}

	@Override
	public Post findOne(int id) {
		return this.getSession().find(Post.class, id);
	}

	@Override
	public List<Post> findAll(int employeeId, int status) throws Exception{
//		CriteriaQuery<Post> criteria = this.getBuilder().createQuery(Post.class);
//		Root<Post> root = criteria.from(Post.class);
//		List<Predicate> predicates = new ArrayList<Predicate>();
////		if (status >= 0) {
////			predicates.add(this.getBuilder().equal(root.get("status"), status));
////		}
//		criteria.select(root).where(predicates.toArray(new Predicate[] {}))
//				.orderBy(this.getBuilder().asc(root.get("id")));
//		return this.getSession().createQuery(criteria).getResultList();
		
		
		StoredProcedureQuery query = this.getSession().createStoredProcedureQuery("sp_get_posts", Post.class)
				.registerStoredProcedureParameter("employeeId", Integer.class, ParameterMode.IN)
				.registerStoredProcedureParameter("status", Integer.class, ParameterMode.IN)
				.registerStoredProcedureParameter("status_code", Integer.class, ParameterMode.OUT)
				.registerStoredProcedureParameter("message_error", String.class, ParameterMode.OUT);

		query.setParameter("employeeId", employeeId);
		query.setParameter("status", status);

		int statusCode = (int) query.getOutputParameterValue("status_code");
		String messageError = query.getOutputParameterValue("message_error").toString();

		switch (StoreProcedureStatusCodeEnum.valueOf(statusCode)) {
		case SUCCESS:
			return query.getResultList();
		case INPUT_INVALID:
			throw new CustomException(HttpStatus.BAD_REQUEST, messageError);
		default:
			throw new Exception(messageError);
		}
	}

	@Override
	public Post spCreatePost(Post post) throws Exception {
		StoredProcedureQuery query = this.getSession().createStoredProcedureQuery("sp_create_post", Post.class)
				.registerStoredProcedureParameter("title", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("content", String.class, ParameterMode.IN)
				.registerStoredProcedureParameter("employeeId", Integer.class, ParameterMode.IN)
				.registerStoredProcedureParameter("status_code", Integer.class, ParameterMode.OUT)
				.registerStoredProcedureParameter("message_error", String.class, ParameterMode.OUT);

		query.setParameter("title", post.getTitle());
		query.setParameter("content", post.getContent());
		query.setParameter("employeeId", post.getEmployeeId());

		int statusCode = (int) query.getOutputParameterValue("status_code");
		String messageError = query.getOutputParameterValue("message_error").toString();

		switch (StoreProcedureStatusCodeEnum.valueOf(statusCode)) {
		case SUCCESS:
			return (Post) query.getResultList().stream().findFirst().orElse(null);
		case INPUT_INVALID:
			throw new CustomException(HttpStatus.BAD_REQUEST, messageError);
		default:
			throw new Exception(messageError);
		}
	}

	
}
