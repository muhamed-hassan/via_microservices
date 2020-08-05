package com.practice.services.helpers;

import java.io.Writer;
import java.util.Set;

import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.IThrottledTemplateProcessor;
import org.thymeleaf.TemplateSpec;
import org.thymeleaf.context.IContext;

public class TestTemplateEngine implements ITemplateEngine {

	@Override
	public IEngineConfiguration getConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String process(String template, IContext context) {
		// TODO Auto-generated method stub
		return "xyz";
	}

	@Override
	public String process(String template, Set<String> templateSelectors, IContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String process(TemplateSpec templateSpec, IContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void process(String template, IContext context, Writer writer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(String template, Set<String> templateSelectors, IContext context, Writer writer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void process(TemplateSpec templateSpec, IContext context, Writer writer) {
		// TODO Auto-generated method stub

	}

	@Override
	public IThrottledTemplateProcessor processThrottled(String template, IContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IThrottledTemplateProcessor processThrottled(String template, Set<String> templateSelectors,
			IContext context) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IThrottledTemplateProcessor processThrottled(TemplateSpec templateSpec, IContext context) {
		// TODO Auto-generated method stub
		return null;
	}

}
