package ar.gfritz;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceFactory;
import com.hazelcast.web.SessionListener;
import com.hazelcast.web.WebFilter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.zkoss.zk.au.http.DHtmlUpdateServlet;
import org.zkoss.zk.ui.http.DHtmlLayoutServlet;

import javax.servlet.DispatcherType;

@Configuration
@ComponentScan("ar.gfritz")
@EnableAutoConfiguration
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	/*
	 * plain URL...
	 */
	@RequestMapping("/")
	@ResponseBody
	String home() {
		return "hi!";
	}

	/*
	 * ZK servlets
	 */
	@Bean
	public ServletRegistrationBean dHtmlLayoutServlet() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("update-uri", "/zkau");
		DHtmlLayoutServlet dHtmlLayoutServlet = new DHtmlLayoutServlet();
		ServletRegistrationBean reg = new ServletRegistrationBean(dHtmlLayoutServlet, "*.zul");
		reg.setLoadOnStartup(1);
		reg.setInitParameters(params);
		return reg;
	}

	@Bean
	public ServletRegistrationBean dHtmlUpdateServlet() {
		Map<String, String> params = new HashMap<String, String>();
		params.put("update-uri", "/zkau*//*");
		ServletRegistrationBean reg = new ServletRegistrationBean(new DHtmlUpdateServlet(), "/zkau*//*");
		reg.setLoadOnStartup(2);
		reg.setInitParameters(params);
		return reg;
	}

	// COmment out the bean (without using the filter), ZK button works, once put in the bean, it breaks.
	//@Bean
	public FilterRegistrationBean hazelcastWebFilter(){
		System.out.println("Registering hazelcast webfilter");

		FilterRegistrationBean hazelcastFilter = new FilterRegistrationBean(new WebFilter());
		hazelcastFilter.addUrlPatterns("*//*");
		hazelcastFilter.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC, DispatcherType.INCLUDE));

		Map<String, String> parameters = new HashMap<>();
		parameters.put("instance-name", "frontier");
		// Name of the distributed map storing your web session objects
		parameters.put("map-name", "clustered-http-sessions");

		// How is your load-balancer configured?
		// Setting "sticky-session" to "true" means all requests of a session
		// are routed to the node where the session is first created.
		// This is excellent for performance.
		// If "sticky-session" is set to "false", then when a session is updated
		// on a node, entries for this session on all other nodes are invalidated.
		// You have to know how your load-balancer is configured before
		// setting this parameter. Default is true.
		parameters.put("sticky-session", "false");

		// Name of session id cookie
		parameters.put("cookie-name", "hazelcast.sessionId");
		parameters.put("debug", "true");


		// Do you want to shutdown HazelcastInstance during
		// web application undeploy process?
		// Default is true.
		parameters.put("shutdown-on-destroy", "true");

		hazelcastFilter.setInitParameters(parameters);
		hazelcastFilter.setAsyncSupported(true);
		return hazelcastFilter;
	}

	@Bean
	public ServletListenerRegistrationBean hazelcastListner(){
		System.out.println("Registering hazelcast listener");
		return new ServletListenerRegistrationBean(new SessionListener());
	}


}
