package com.truong.dao.impl;


import java.util.ArrayList;
import java.util.List;



import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.truong.dao.AbstractDao;
import com.truong.dao.EmployeeDao;
import com.truong.entity.Employee;

import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


@Repository("employeeDao")
@Transactional
public class EmployeeDaoImpl extends AbstractDao<Integer, Employee> implements EmployeeDao{

	@Override
	public Employee findOne(int id) {
		return this.getSession().find(Employee.class, id);
	}

	@Override
	public Employee findByUsername(String username) {
		CriteriaQuery<Employee> criteria = this.getBuilder().createQuery(Employee.class);

		Root<Employee> root = criteria.from(Employee.class);
		criteria.select(root).where(this.getBuilder().equal(root.get("username"), username));
		return this.getSession().createQuery(criteria).uniqueResult();
	}

	@Override
	public List<Employee> findAll() {
		CriteriaQuery<Employee> criteria = this.getBuilder().createQuery(Employee.class);
		Root<Employee> root = criteria.from(Employee.class);
		List<Predicate> predicates = new ArrayList<Predicate>();
//		if (status >= 0) {
//			predicates.add(this.getBuilder().equal(root.get("status"), status));
//		}
		criteria.select(root).where(predicates.toArray(new Predicate[] {}))
				.orderBy(this.getBuilder().asc(root.get("id")));
		return this.getSession().createQuery(criteria).getResultList();
	}
	
}
