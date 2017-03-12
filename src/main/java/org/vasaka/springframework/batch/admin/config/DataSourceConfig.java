package org.vasaka.springframework.batch.admin.config;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@ComponentScan(basePackages = "org.vasaka.springframework.batch.admin.service")
@PropertySource("classpath:${ENVIRONMENT:default}.properties")
public class DataSourceConfig {

	@Autowired
	private Environment env;
	
	@Value("${batch.drop.script}")
	private Resource dropScript;

	@Value("${batch.schema.script}")
	private Resource schemaScript;

	@Bean(destroyMethod = "close")
	public DataSource dataSource() {
		BasicDataSource dataSource = new BasicDataSource();
		dataSource.setDriverClassName(env.getProperty("batch.jdbc.driver"));
		dataSource.setUrl(env.getProperty("batch.jdbc.url"));
		dataSource.setUsername(env.getProperty("batch.jdbc.user"));
		dataSource.setPassword(env.getProperty("batch.jdbc.password"));
		dataSource.setValidationQuery(env.getProperty("batch.jdbc.validationQuery"));
		dataSource.setTestWhileIdle(Boolean.valueOf(env.getProperty("batch.jdbc.testWhileIdle")));
		return dataSource;
	}

	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(dataSource());
	}

	@Bean
	public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
		final DataSourceInitializer initializer = new DataSourceInitializer();
		initializer.setEnabled(Boolean.valueOf(env.getProperty("batch.data.source.init")));
		initializer.setDataSource(dataSource);
		initializer.setDatabasePopulator(databasePopulator());
		return initializer;
	}

	private DatabasePopulator databasePopulator() {
		final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.setIgnoreFailedDrops(true);
		populator.setContinueOnError(true);
		populator.addScript(dropScript);
		populator.addScript(schemaScript);
		return populator;
	}
}
