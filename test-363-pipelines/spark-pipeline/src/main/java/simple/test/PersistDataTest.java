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

import java.lang.String;

import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.sql.types.DataTypes;

/**
 * Performs the business logic for PersistDataTest.
 *
 * Because this class is {@link ApplicationScoped}, exactly one managed singleton instance will exist
 * in any deployment.
 *
 * GENERATED STUB CODE - PLEASE ***DO*** MODIFY
 *
 * Originally generated from: templates/data-delivery-spark/synchronous.processor.impl.java.vm
 */
@ApplicationScoped
public class PersistDataTest extends PersistDataTestBase {

    private static final Logger logger = LoggerFactory.getLogger(PersistDataTest.class);
    public static String TABLE_NAME = "testPersistData";

    public PersistDataTest(){
        super("synchronous",getDataActionDescriptiveLabel());
    }

    /**
    * Provides a descriptive label for the action that can be used for logging (e.g., provenance details).
    *
    * @return descriptive label
    */
    private static String getDataActionDescriptiveLabel(){
        // TODO: replace with descriptive label
        return"PersistDataTest";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String executeStepImpl(String inbound) {
        logger.error("Persist Data ...");
        StructType structType = new StructType();
        structType = structType.add("inbound message", DataTypes.StringType, true);
        // prepare data
        List<Row> listRows = new ArrayList<>();
        listRows.add(RowFactory.create(inbound));
        Dataset<Row> df = sparkSession.createDataFrame(listRows, structType);
        // save to table
        String tableName = config.outputDirectory() + TABLE_NAME;
        saveDataset(df, tableName);
        Dataset<Row> content = sparkSession.read().format("delta").load().toDF();
        content.show();
        return inbound;
    }

}
