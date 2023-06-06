#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Container image that runs your code
FROM maven:latest

MAINTAINER wuyfee "wyf_mohen@163.com"

EXPOSE  9082
COPY src /src
COPY pom.xml /pom.xml

#RUN mvn clean install \
#    && ls /target \
#    && mv /target/rocketmq-test-tools-1.0-SNAPSHOT-jar-*.jar ./rocketmq-test-tools.jar \
#    && rm -rf /pom.xml /src /target \
#    && chmod 777 /rocketmq-test-tools.jar

ENTRYPOINT ["/bin/sh", "-c", "echo testRepo:$0 action:$1 version:$2 askConfig:$3 velauxUsername:$4 velauxPassword:$5 chartGit:$6 chartBranch:$7 chartPath:$8 testCodeGit:$9 testCodeBranch:${10} testCodePath:${11} testCmdBase:${12} jobIndex:${13} helmValue:${14} ${15} ${16} ${17} $*"]
#ENTRYPOINT ["java", "-jar", "/rocketmq-test-tools.jar", "-testRepo=${1}", "-action=${2n}", "-version=${3}", "-askConfig=${4}", "-velauxUsername=${5}", "-velauxPassword=${6}", "-chartGit=${7}", "-chartBranch=${8}", "-chartPath=${9}", "-testCodeGit=${10}", "-testCodeBranch=${11}", "-testCodePath=${12}", "-testCmdBase=${13}", "-jobIndex=${14}", "-helmValue=${15}"]
#ENTRYPOINT ["/bin/sh", "-c","java -jar /rocketmq-test-tools.jar -testRepo=${3} -action=${3} -version=${4} -askConfig=${5} -velauxUsername=${6} -velauxPassword=${7} -chartGit=${8} -chartBranch=${9} -chartPath=${10} -testCodeGit=${11} -testCodeBranch=${12} -testCodePath=${13} -jobIndex=${14} -helmValue=${15}"]
#ENTRYPOINT ["java", "-jar", "/rocketmq-test-tools.jar", "-testRepo=${testRepo} -action=${action} -version=${version} -askConfig=${askConfig} -velauxUsername=${velauxUsername} -velauxPassword=${velauxPassword} -chartGit=${chartGit} -chartBranch=${chartBranch} -chartPath=${chartPath} -testCodeGit=${testCodeGit} -testCodeBranch=${testCodeBranch} -testCodePath=${testCodePath} -testCmdBase=${testCmdBase} -jobIndex=${jobIndex} -helmValue=${helmValue}"]
#ENTRYPOINT ["java", "-jar", "/rocketmq-test-tools.jar", "-testRepo=${testRepo}", "-action=${action}", "-version=${version}", "-askConfig=${askConfig}", "-velauxUsername=${velauxUsername}", "-velauxPassword=${velauxPassword}", "-chartGit=${chartGit}", "-chartBranch=${chartBranch}", "-chartPath=${chartPath}", "-testCodeGit=${testCodeGit}", "-testCodeBranch=${testCodeBranch}", "-testCodePath=${testCodePath}", "-testCmdBase=${testCmdBase}", "-jobIndex=${jobIndex}", "-helmValue=${helmValue}"]