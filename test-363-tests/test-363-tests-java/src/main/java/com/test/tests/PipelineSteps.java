package com.test.tests;

/*-
 * #%L
 * test-363::Tests::Java
 * %%
 * Copyright (C) 2021 Booz Allen
 * %%
 * All Rights Reserved. You may not copy, reproduce, distribute, publish, display, 
 * execute, modify, create derivative works of, transmit, sell or offer for resale, 
 * or in any way exploit any part of this solution without Booz Allen Hamilton’s 
 * express written permission.
 * #L%
 */

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class PipelineSteps {

    private static final Logger logger = LoggerFactory.getLogger(PipelineSteps.class);
    List<File> listOfFiles = new ArrayList<>();
    Boolean jobRequestFailed;
    List<Pod> podList;
    List<HasMetadata> hasMetadata;

    @Before("@pipeline")
    public void setup() {
        this.jobRequestFailed = false;
        this.podList = new ArrayList<>();
        this.hasMetadata = new ArrayList<>();
    }

    @After("@pipeline")
    public void cleanup() {
    }

    @When("the job is triggered")
    public void theJobIsTriggered() throws IOException, InterruptedException {
        getFiles();
    }

    @Then("the pipeline is executed")
    public void thePipelineIsExecuted() {
        Assert.assertTrue(String.format("Pod list size should be greater than  %d", podList.size()), podList.size() > 0);
    }

    private void getFiles() throws IOException, InterruptedException {

        File dir = new File("./pipelines");
        addFilesForFolder(dir);

        logger.info(String.format("Got %d application.yaml file(s) to process", listOfFiles.size()));

        for (File file : listOfFiles) {

          try (KubernetesClient client = new KubernetesClientBuilder().build()) {

              //Initializing Informer
              this.initializeInformer(client);

              List<HasMetadata> metadata = client
                  .load(new FileInputStream(file))
                  .get();

              this.hasMetadata.addAll(metadata);
              logger.info(String.format("has meta data size is %d", hasMetadata.size()));

              //Creating/Replacing resource here
              metadata.forEach(o -> logger.info("Creating resource " + o.getMetadata().getName()));
              client.resourceList(metadata).inNamespace("default").createOrReplace();
              TimeUnit.MINUTES.sleep(2);

          }
        }
    }

    private void initializeInformer(KubernetesClient client) {

        client.pods().inNamespace("default").inform(new ResourceEventHandler<>() {

            @Override
            public void onAdd(Pod pod) {
                addToPodList(pod);
            }

            @Override
            public void onUpdate(Pod oldPod, Pod newPod) {
                addToPodList(oldPod);
            }

            @Override
            public void onDelete(Pod pod, boolean deletedFinalStateUnknown) {
                addToPodList(pod);
            }

        }, 30 * 1000L);

        logger.info("Informer initialized.");
    }


    private void addFilesForFolder(final File folder) {
        for (final File fileEntry : Objects.requireNonNull(folder.listFiles())) {
            if (fileEntry.isDirectory()) {
                addFilesForFolder(fileEntry);
            } else {
                listOfFiles.add(fileEntry);
            }
        }
    }

    private void addToPodList(Pod pod) {

        boolean anyMatch = this.hasMetadata.stream().anyMatch(o -> pod.getMetadata().getName().contains(o.getMetadata().getName()));
        if (anyMatch) {
            logger.info("Pod " + pod.getMetadata().getName() + " added to pod list");
            this.podList.add(pod);
        }
    }

}
