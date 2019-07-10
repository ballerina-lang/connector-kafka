// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import ballerina/log;

kafka:ProducerConfig producerConfigs = {
    // Here we create a producer configs with SSL parameters
    // clientId - used for broker side logging.
    // acks - number of acknowledgments for request complete,
    // noRetries - number of retries if record send fails.
    // securityProtocol - communication protocol
    // sslTruststoreLocation - location of truststore file
    // sslTruststorePassword - truststore password
    // sslKeystoreLocation - location of keystore file
    // sslKeystorePassword - keystore password
    // sslKeyPassword - password of the private key in the key store file
    bootstrapServers: "localhost:9093",
    clientId:"basic-producer",
    acks:"all",
    noRetries:3,
    sslEnabledProtocols:"TLSv1.2,TLSv1.1,TLSv1",
    securityProtocol:"SSL",
    sslTruststoreLocation:"<FILE-PATH>/kafka.client.truststore.jks",
    sslTruststorePassword:"test1234",
    sslKeystoreLocation:"<FILE-PATH>/kafka.client.keystore.jks",
    sslKeystorePassword:"test1234",
    sslKeyPassword:"test1234"
};

kafka:Producer kafkaProducer = new(producerConfigs);

public function main () {
    string msg = "Hello World, Ballerina";
    byte[] serializedMsg = msg.toByteArray("UTF-8");
    var sendResult = kafkaProducer->send(serializedMsg, "test-kafka-topic");
    if (sendResult is error) {
        log:printError("Kafka producer failed to send data", err = sendResult);
    }
    var flushResult = kafkaProducer->flushRecords();
    if (flushResult is error) {
        log:printError("Kafka producer failed to flush the records", err = flushResult);
    }
}

