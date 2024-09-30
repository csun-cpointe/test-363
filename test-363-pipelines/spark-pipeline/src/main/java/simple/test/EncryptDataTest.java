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

import java.util.Set;
import simple.test.record.Person;
import simple.test.record.PersonSchema;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

import com.boozallen.aissemble.core.metadata.MetadataModel;


import java.util.Set;
import simple.test.record.Person;
import simple.test.record.PersonSchema;
import com.boozallen.aissemble.data.encryption.policy.config.EncryptAlgorithm;
import org.apache.spark.sql.types.DataTypes;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Paths;


import com.boozallen.aissemble.data.encryption.policy.EncryptionPolicy;
import com.boozallen.aissemble.data.encryption.policy.EncryptionPolicyManager;
import com.boozallen.aissemble.data.encryption.AiopsEncrypt;
import com.boozallen.aissemble.data.encryption.SimpleAesEncrypt;
import com.boozallen.aissemble.data.encryption.VaultEncrypt;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.functions;
import org.apache.spark.sql.types.StructType;


import static org.apache.spark.sql.functions.col;
import static org.apache.spark.sql.functions.lit;

/**
 * Performs the business logic for EncryptDataTest.
 *
 * Because this class is {@link ApplicationScoped}, exactly one managed singleton instance will exist
 * in any deployment.
 *
 * GENERATED STUB CODE - PLEASE ***DO*** MODIFY
 *
 * Originally generated from: templates/data-delivery-spark/synchronous.processor.impl.java.vm
 */
@ApplicationScoped
public class EncryptDataTest extends EncryptDataTestBase {

    private static final Logger logger = LoggerFactory.getLogger(EncryptDataTest.class);

    public EncryptDataTest(){
        super("synchronous",getDataActionDescriptiveLabel());
    }

    /**
    * Provides a descriptive label for the action that can be used for logging (e.g., provenance details).
    *
    * @return descriptive label
    */
    private static String getDataActionDescriptiveLabel(){
        // TODO: replace with descriptive label
        return"EncryptDataTest";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void executeStepImpl(Set<Person> inbound) {
        // TODO: Add your business logic here for this step!
        logger.error("Implement executeStepImpl(..) or remove this pipeline step!");

    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected MetadataModel createProvenanceMetadata(String resource,String subject,String action){
        // TODO: Add any additional provenance-related metadata here
        return new MetadataModel(resource,subject,action,Instant.now());
    }

    protected Set<Person> checkAndApplyEncryptionPolicy(Set<Person> inbound) {
        // Check Encryption Policy
        Set<Person> datasetWithEncryptionPolicyApplied = inbound;
        EncryptionPolicyManager encryptionPolicyManager = EncryptionPolicyManager.getInstance();
        String filePath = encryptionPolicyManager.getPoliciesLocation();

        if(Files.isDirectory(Paths.get(filePath))) {
            Map<String, EncryptionPolicy> policies = encryptionPolicyManager.getEncryptPolicies();

            if(!policies.isEmpty()) {
                for(EncryptionPolicy encryptionPolicy: policies.values()) {
                    if(stepPhase.equalsIgnoreCase(encryptionPolicy.getEncryptPhase())){
                        List<String> encryptFields = encryptionPolicy.getEncryptFields();
                        List<Row> rows = datasetWithEncryptionPolicyApplied.stream()
                                .map(PersonSchema::asRow)
                                .collect(Collectors.toList());
                        PersonSchema schema = new PersonSchema();
                        Dataset<Row> ds = sparkSession.createDataFrame(rows, schema.getStructType());

                        System.out.println("=============== before encryption ===================");
                        ds.show(false);
                        List<String> datasetFields = Arrays.asList(ds.columns());
                        List<String> fieldIntersection = encryptFields.stream()
                                .filter(datasetFields::contains)
                                .collect(Collectors.toList());

                        // Registered UDF calls prefer String over Enum
                        String encryptAlgorithm = encryptionPolicy.getEncryptAlgorithm().toString();

                        for(String encryptField: fieldIntersection) {
                            System.out.println("-------- encryptField: " + encryptField);
                            ds = ds.withColumn(encryptField,
                                    functions.callUDF( "encryptUDF", col(encryptField), lit(encryptAlgorithm)));
                            // Encoder<Person> recordEncoder = Encoders.bean(Person.class);
                            // datasetWithEncryptionPolicyApplied = new HashSet<>(ds.as(recordEncoder).collectAsList());
                        }
                        System.out.println("=============== after encryption ===================");
                        ds.show(false);
                    }
                }
            }
        }
        return datasetWithEncryptionPolicyApplied;
    }
}
