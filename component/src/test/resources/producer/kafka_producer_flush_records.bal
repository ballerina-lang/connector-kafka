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

string topic = "producer-flush-records-test-topic";

kafka:ProducerConfig producerConfigs = {
    bootstrapServers: "localhost:9094",
    clientId: "flush-records-producer",
    acks: "all",
    noRetries: 3
};

kafka:Producer kafkaProducer = new(producerConfigs);

function funcKafkaTestFlush() returns boolean {
    string msg = "Hello World";
    byte[] byteMsg = msg.toByteArray("UTF-8");
    var result = kafkaProducer->send(byteMsg, "test");

    if (result is error) {
        return false;
    }

    result = kafkaProducer->flushRecords();
    if (result is error) {
        return false;
    }

    return true;
}
