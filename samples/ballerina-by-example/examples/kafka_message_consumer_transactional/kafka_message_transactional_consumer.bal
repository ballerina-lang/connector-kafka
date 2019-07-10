// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import wso2/kafka;
import ballerina/io;
import ballerina/encoding;
import ballerina/log;

kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers:"localhost:9092",
    groupId:"group-id",
    topics:["test-kafka-topic"],
    pollingInterval:1000,
    autoCommit:false,
    isolationLevel:"read_committed"
};

listener kafka:Consumer consumer = new(consumerConfigs);

service kafkaService on consumer {

    resource function onMessage(kafka:Consumer kafkaConsumer, kafka:ConsumerRecord[] records) {
        // Dispatched set of Kafka records to service, We process each one by one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
        // Commit offsets returned for returned records, marking them as consumed.
        var commitResult = kafkaConsumer->commit();
        if (commitResult is error) {
            log:printError("Error occurred while committing the offsets for the consumer ", err = commitResult);
        }
    }
}

function processKafkaRecord(kafka:ConsumerRecord kafkaRecord) {
    byte[] serializedMsg = kafkaRecord.value;
    string msg = encoding:byteArrayToString(serializedMsg);
    // Print the retrieved Kafka record.
    io:println("Topic: " + kafkaRecord.topic + " Received Message: " + msg);
}
