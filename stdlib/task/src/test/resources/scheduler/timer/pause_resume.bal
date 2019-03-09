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

import ballerina/task;
import ballerina/runtime;

task:TimerConfiguration configuration = {
    interval: 1000,
    initialDelay: 1000
};

boolean isPaused = false;
boolean isResumed = false;

function testAttach() {
    task:Scheduler timer = new(configuration);
    _ = timer.attach(timerService);
    _ = timer.start();
    var result = timer.pause();
    if (result is error) {
        return;
    } else {
        isPaused = true;
    }
    result = timer.resume();
    if (result is error) {
        return;
    } else {
        isResumed = true;
    }
}

function getIsPaused() returns boolean {
    return isPaused;
}

function getIsResumed() returns boolean {
    return isResumed;
}

service timerService = service {
    resource function onTrigger() {
        // Do nothing
    }
};
