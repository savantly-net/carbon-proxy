package net.savantly.metrics.carbonProxy.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Configuration
public class WebConfiguration {

	@RequestMapping("/")
	public String redirectRootPath(){
		return "redirect:/hawtio/";
	}
}
