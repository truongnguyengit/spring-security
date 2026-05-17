package com.truong.dao;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import org.hibernate.Session;

import com.truong.entity.BaseEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
public abstract class AbstractDao<PK extends Serializable, T> {

	private final Class<T> persistentClass;

	@SuppressWarnings("unchecked")
	public AbstractDao() {
		this.persistentClass = (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
				.getActualTypeArguments()[1];
	}

	@PersistenceContext
    private EntityManager entityManager;

	protected Session getSession() {
		
		return entityManager.unwrap(Session.class);
	}

	protected CriteriaBuilder getBuilder() {
		return this.getSession().getCriteriaBuilder();
	}

	public T getByKey(PK key) {
		return (T) getSession().get(persistentClass, key);
	}

	public void persist(T entity) {
		getSession().persist(entity);
	}

	public void delete(T entity) {
		getSession().delete(entity);
	}


	public void update(BaseEntity entity) {
		this.getSession().update(entity);
	}
	
	public void merge(BaseEntity entity) {
		this.getSession().merge(entity);
	}

	public void delete(BaseEntity entity) {
		this.getSession().delete(entity);
	}

	public void refresh(BaseEntity entity) {
		this.getSession().refresh(entity);
	}

	public void flush() {
		this.getSession().flush();
	}

	
}
