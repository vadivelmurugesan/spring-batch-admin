package org.vasaka.springframework.batch.admin.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.admin.domain.support.ISO8601DateFormatWithMilliSeconds;
import org.springframework.batch.admin.domain.support.VariableTypeJackson2ObjectMapperFactoryBean;
import org.springframework.batch.admin.web.RestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.HateoasSortHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssemblerArgumentResolver;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

@Configuration
@EnableWebMvc
@Import(RestConfiguration.class) // For loading spring batch admin's controller
@ComponentScan(basePackages = "org.vasaka.springframework.batch.admin.controller")
@EnableSpringDataWebSupport
public class WebConfig extends WebMvcConfigurerAdapter {
	@Bean
	public ViewResolver viewResolver() {
		InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
		viewResolver.setViewClass(JstlView.class);
		viewResolver.setPrefix("/WEB-INF/views/");
		viewResolver.setSuffix(".jsp");

		return viewResolver;
	}

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {

		argumentResolvers.add(sortResolver());
		argumentResolvers.add(pageableResolver());
		argumentResolvers.add(sortResolver());
	}

	@Bean
	public HateoasSortHandlerMethodArgumentResolver sortResolver() {
		return new HateoasSortHandlerMethodArgumentResolver();
	}

	@Bean
	public PagedResourcesAssemblerArgumentResolver pagedResourcesAssemblerArgumentResolver() {
		return new PagedResourcesAssemblerArgumentResolver(pageableResolver(), null);
	}

	@Bean
	public HateoasPageableHandlerMethodArgumentResolver pageableResolver() {
		return new HateoasPageableHandlerMethodArgumentResolver(sortResolver());
	}

	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(jsonConverter());
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public MappingJackson2HttpMessageConverter jsonConverter() {
		Jackson2ObjectMapperBuilder builder = Jackson2ObjectMapperBuilder.xml();
		builder.indentOutput(true);
		MappingJackson2HttpMessageConverter jacksonConverter = new MappingJackson2HttpMessageConverter(builder.build());
		jacksonConverter.setSupportedMediaTypes(Arrays.asList(MediaType.valueOf("application/json")));
		jacksonConverter.setObjectMapper(jacksonObjectMapper());
		return jacksonConverter;
	}

	@Bean
	public ObjectMapper jacksonObjectMapper() {
		VariableTypeJackson2ObjectMapperFactoryBean factoryBean = new VariableTypeJackson2ObjectMapperFactoryBean();
		factoryBean.setFeaturesToDisable(SerializationFeature.WRITE_DATE_KEYS_AS_TIMESTAMPS);
		factoryBean.setDateFormat(ISO8601DateFormatWithMilliSeconds.getDateInstance());
		Map<Class<?>, Class<?>> mixIns = new HashMap<Class<?>, Class<?>>();
		mixIns.put(org.springframework.batch.core.JobParameters.class, org.springframework.batch.admin.domain.support.JobParametersJacksonMixIn.class);
		mixIns.put(org.springframework.batch.core.JobParameter.class, org.springframework.batch.admin.domain.support.JobParameterJacksonMixIn.class);
		mixIns.put(org.springframework.batch.admin.domain.StepExecutionHistory.class,
				org.springframework.batch.admin.domain.support.StepExecutionHistoryJacksonMixIn.class);
		mixIns.put(org.springframework.batch.core.ExitStatus.class, org.springframework.batch.admin.domain.support.ExitStatusJacksonMixIn.class);
		factoryBean.setMixIns(mixIns);
		factoryBean.afterPropertiesSet();
		ObjectMapper objectMapper = factoryBean.getObject();
		return objectMapper;
	}

}