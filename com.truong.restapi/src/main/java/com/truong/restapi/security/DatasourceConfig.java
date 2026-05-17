package com.truong.restapi.security;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.truong.configuration.ApplicationPropertieConfig;
import com.truong.restapi.config.ReplicationRoutingDataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import jakarta.annotation.PostConstruct;


@Configuration
@ComponentScan("com.truong")
@EnableTransactionManagement
@EnableAutoConfiguration
public class DatasourceConfig {

	@Autowired
	private ApplicationPropertieConfig config;

	@Autowired
	private Environment environment;

	@PostConstruct
	void initSetting() {
		System.out.println("=====Loading config=====");
		System.out.println(String.format("Datasource master url: %s", config.getDatasourceMasterUrl()));
		System.out.println(String.format("Datasource slave url: %s", config.getDatasourceSlaveUrl()));
		System.out.println("=====End=====");
	}

//	@Bean
//	public DataSource dataSource() {
//		HikariConfig hikariConfig = this.initHikariPoolingConfig("hikari-pool");
//		hikariConfig.setJdbcUrl(config.getDatasourceUrl());
//		hikariConfig.setUsername(config.getDatasourceUsername());
//		hikariConfig.setPassword(config.getDatasourcePassword());
//
//		HikariDataSource hikariPoolingDataSource = new HikariDataSource(hikariConfig);
//
//		return hikariPoolingDataSource;
//	}
	
	
	@Bean
	DataSource writeOnlyDataSource() {
		HikariConfig hikariConfig = this.initHikariPoolingConfig("hikari-master-pool");
		hikariConfig.setJdbcUrl(config.getDatasourceMasterUrl());
		hikariConfig.setUsername(config.getDatasourceMasterUsername());
		hikariConfig.setPassword(config.getDatasourceMasterPassword());
		hikariConfig.setReadOnly(false);
		
		return new HikariDataSource(hikariConfig);
	}
	
	@Bean
	DataSource readOnlyDataSource() {
		HikariConfig hikariConfig = this.initHikariPoolingConfig("hikari-slave-pool");
		hikariConfig.setJdbcUrl(config.getDatasourceSlaveUrl());
		hikariConfig.setUsername(config.getDatasourceSlaveUsername());
		hikariConfig.setPassword(config.getDatasourceSlavePassword());
		hikariConfig.setReadOnly(true);
		
		return new HikariDataSource(hikariConfig);
	}
	
	@Bean
    DataSource routingDataSource() {
        ReplicationRoutingDataSource routingDataSource = new ReplicationRoutingDataSource();

        Map<Object, Object> dataSourceMap = new HashMap<>();
        dataSourceMap.put("write", writeOnlyDataSource());
        dataSourceMap.put("read", readOnlyDataSource());
        routingDataSource.setTargetDataSources(dataSourceMap);
        routingDataSource.setDefaultTargetDataSource(writeOnlyDataSource());

        return routingDataSource;
    }
	
	/*
	 * @Primary, điều này có nghĩa là nó sẽ được chọn làm DataSource mặc định nếu có nhiều DataSource bean trong ứng dụng
	 * Trong Spring Framework, @Qualifier là một annotation được sử dụng để xác định rõ ràng bean nào nên được sử dụng trong trường hợp có nhiều bean cùng loại. Annotation này giúp Spring hiểu được bạn muốn inject bean nào khi có nhiều bean cùng loại được khai báo.
	 */
	@Primary
    @Bean
    @DependsOn({"writeOnlyDataSource", "readOnlyDataSource", "routingDataSource"})
    DataSource dataSource() {
        return new LazyConnectionDataSourceProxy(routingDataSource());
    }
	
	private Properties hibernateProperties() {
		Properties properties = new Properties();
		// Đồng bộ hóa các key cấu hình theo đúng chuẩn định dạng String của Hibernate
		properties.put("hibernate.dialect", environment.getRequiredProperty("hibernate.dialect"));
		properties.put("hibernate.show_sql", environment.getRequiredProperty("hibernate.show_sql"));
		properties.put("hibernate.format_sql", environment.getRequiredProperty("hibernate.format_sql"));
		properties.put("hibernate.current_session_context_class", environment.getRequiredProperty("hibernate.current_session_context_class"));
		
		// Thêm dòng này để Spring Data JPA quản lý Transaction đồng bộ với Hibernate cũ của bạn
		properties.put("hibernate.transaction.coordinator_class", "jdbc"); 
		return properties;
	}

	@Bean
	@Primary // Đánh dấu đây là EntityManager mặc định cho toàn bộ dự án
	public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
		LocalContainerEntityManagerFactoryBean em = 
				new LocalContainerEntityManagerFactoryBean();
		
		// Gán Lazy Connection Proxy DataSource (đã bọc routing của bạn) vào đây
		em.setDataSource(dataSource());
		
		// Quét các class @Entity của bạn
		em.setPackagesToScan(new String[] { "com.truong.entity" });

		// Cấu hình để JPA sử dụng Hibernate làm Engine xử lý ngầm
		org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter vendorAdapter = 
				new org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter();
		em.setJpaVendorAdapter(vendorAdapter);

		// Nạp các thuộc tính cấu hình Hibernate từ file properties của bạn vào
		em.setJpaProperties(hibernateProperties());

		return em;
	}
	
	@Bean
	@Primary
	public JpaTransactionManager transactionManager(
			LocalContainerEntityManagerFactoryBean entityManagerFactory) {
		JpaTransactionManager transactionManager = 
				new JpaTransactionManager();
		
		transactionManager.setEntityManagerFactory(entityManagerFactory.getObject());
		return transactionManager;
	}

	private HikariConfig initHikariPoolingConfig(String poolName) {
		HikariConfig hikariConfig = new HikariConfig();

		hikariConfig.setPoolName(poolName);
		hikariConfig.setDriverClassName(config.getDriverClassname());
		hikariConfig.setConnectionTimeout(config.getHikariCP_ConnectionTimeout());
		hikariConfig.setIdleTimeout(config.getHikariCP_IdleTimeout());
		hikariConfig.setMaximumPoolSize(config.getHikariCP_MaximumPoolSize());
		hikariConfig.setMinimumIdle(config.getHikariCP_MinimumIdle());
		hikariConfig.setMaxLifetime(config.getHikariCP_MaxLifetime());
		hikariConfig.setTransactionIsolation(String.valueOf(Connection.TRANSACTION_READ_COMMITTED));

		hikariConfig.addDataSourceProperty("cachePrepStmts", config.getHikariCP_CachePrepStmts());
		hikariConfig.addDataSourceProperty("prepStmtCacheSize", config.getHikariCP_PrepStmtCacheSize());
		hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", config.getHikariCP_PrepStmtCacheSqlLimit());

		return hikariConfig;
	}
}
