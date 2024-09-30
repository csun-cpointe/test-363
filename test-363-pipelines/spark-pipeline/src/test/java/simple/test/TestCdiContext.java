package simple.test;

/*-
 * #%L
 * test-363::Pipelines::Spark Pipeline
 * %%
 * Copyright (C) 2021 Booz Allen
 * %%
 * All Rights Reserved. You may not copy, reproduce, distribute, publish, display, 
 * execute, modify, create derivative works of, transmit, sell or offer for resale, 
 * or in any way exploit any part of this solution without Booz Allen Hamilton’s 
 * express written permission.
 * #L%
 */

import java.util.List;

import simple.test.cdi.PipelinesCdiContext;
import com.boozallen.aissemble.core.cdi.CdiContext;

import io.smallrye.reactive.messaging.memory.InMemoryConnector;


/**
 * Provides the set of CDI context values needed to run Cucumber tests.
 *
 * GENERATED STUB CODE - PLEASE ***DO*** MODIFY
 *
 * Originally generated from: templates/cucumber.test.cdi.context.java.vm 
 */
public class TestCdiContext extends PipelinesCdiContext {

	@Override
	public List<Class<?>> getCdiClasses() {
		List<Class<?>> classes = super.getCdiClasses();
		classes.add(InMemoryConnector.class);
		return classes;
	}

}
