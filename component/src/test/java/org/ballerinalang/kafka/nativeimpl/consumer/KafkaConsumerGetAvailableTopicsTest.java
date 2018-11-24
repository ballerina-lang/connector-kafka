/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.kafka.nativeimpl.consumer;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BStringArray;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

/**
 * Test cases for ballerina.net.kafka consumer for get list of available topics
 * using getAvailableTopics() native function.
 */
public class KafkaConsumerGetAvailableTopicsTest {
    private CompileResult result;
    private static File dataDir;
    private static KafkaCluster kafkaCluster;

    @BeforeClass
    public void setup() throws IOException {
        result = BCompileUtil.compile("consumer/kafka_consumer_get_available_topics.bal");
        Properties prop = new Properties();
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true)
                .deleteDataUponShutdown(true).withKafkaConfiguration(prop).addBrokers(1).startup();
        kafkaCluster.createTopic("test", 1, 1);
    }

    @Test(
            description = "Test functionality of getAvailableTopics() function",
            sequential = true
    )
    public void testKafkaConsumerGetAvailableTopics () {
        CountDownLatch completion = new CountDownLatch(1);
        kafkaCluster.useTo().produceStrings("test", 10, completion::countDown, () -> "test_string");
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
        BValue[] inputBValues = {};
        BValue[] returnBValues = BRunUtil.invoke(result, "funcKafkaConnect", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        // getting kafka endpoint
        BValue consumerEndpoint = returnBValues[0];
        inputBValues = new BValue[]{consumerEndpoint};
        returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopics", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returnBValues[0]).size(), 1);
        Assert.assertEquals(((BStringArray) returnBValues[0]).get(0), "test");

        completion = new CountDownLatch(1);
        kafkaCluster.useTo().produceStrings("test-2", 10, completion::countDown, () -> "test_string");
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
        BValue duration = new BInteger(1111);
        inputBValues = new BValue[]{consumerEndpoint, duration};
        returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopicsWithDuration", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returnBValues[0]).size(), 2);
        Assert.assertEquals(((BStringArray) returnBValues[0]).get(0), "test-2");
        Assert.assertEquals(((BStringArray) returnBValues[0]).get(1), "test");

        completion = new CountDownLatch(1);
        kafkaCluster.useTo().produceStrings("test-2", 10, completion::countDown, () -> "test_string");
        try {
            completion.await();
        } catch (Exception ex) {
            //Ignore
        }
        // Obtain consumer with Invalid API timeout value.
        returnBValues = BRunUtil.invoke(result, "funcKafkaGetNoTimeoutConsumer", new BValue[]{});
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BMap);
        // getting kafka endpoint
        consumerEndpoint = returnBValues[0];
        duration = new BInteger(-10000);
        inputBValues = new BValue[]{consumerEndpoint, duration};
        returnBValues = BRunUtil.invoke(result, "funcKafkaGetAvailableTopicsWithDuration", inputBValues);
        Assert.assertEquals(returnBValues.length, 1);
        Assert.assertTrue(returnBValues[0] instanceof BStringArray);
        Assert.assertEquals(((BStringArray) returnBValues[0]).size(), 2);
        Assert.assertEquals(((BStringArray) returnBValues[0]).get(0), "test-2");
        Assert.assertEquals(((BStringArray) returnBValues[0]).get(1), "test");
    }

    @AfterClass
    public void tearDown() {
        if (kafkaCluster != null) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
            boolean delete = dataDir.delete();
            // If files are still locked and a test fails: delete on exit to allow subsequent test execution
            if (!delete) {
                dataDir.deleteOnExit();
            }
        }
    }

    private static KafkaCluster kafkaCluster() {
        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory("cluster-kafka-consumer");
        kafkaCluster = new KafkaCluster().usingDirectory(dataDir).withPorts(2185, 9094);
        return kafkaCluster;
    }
}
