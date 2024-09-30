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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.String;
import java.util.Set;
import java.util.HashSet;

import simple.test.record.Person;
import simple.test.record.PersonSchema;
import simple.test.dictionary.AgeType;


import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.spi.CDI;

import simple.test.pipeline.PipelineBase;

/**
 * Configures the steps needed to run a Spark-based implementation for SparkPipeline.
 *
 * This pipeline serves the following purpose: ${pipeline.description}.
 *
 * Please **DO** modify with your customizations, as appropriate.
 *
 * Originally generated from: templates/pipeline.driver.impl.java.vm 
 */
public class SparkPipelineDriver extends SparkPipelineBaseDriver {

  private static final Logger logger = LoggerFactory.getLogger(SparkPipelineDriver.class);
  
  public static void main(String[] args) {
    logger.info("STARTED: {} driver", "SparkPipeline");
    SparkPipelineBaseDriver.main(args);

    PipelineBase.getInstance().recordPipelineLineageStartEvent();
    Set<Person> input = new HashSet<>();

    for (var i=1; i<11; i++) {
      Person person = new Person();
      person.setEmployment("Student" + i);
      person.setName("John Smith " + i);
      person.setSsn("111-222-000" + i);
      int age = (i*6) % 11;
      System.out.println("-------- age: " + age);
      person.setAge(new AgeType(age));
      try {
        person.validate();
        input.add(person);
      } catch (Exception e) {
        logger.warn("Person validation failed: " + person);
      }
    }

    final EncryptDataTest encryptDataTest = CDI.current().select(EncryptDataTest.class, new Any.Literal()).get();
    encryptDataTest.executeStep(input);
    PipelineBase.getInstance().recordPipelineLineageCompleteEvent();
  }
}
