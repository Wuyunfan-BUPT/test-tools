/*
 * #
 * # Licensed to the Apache Software Foundation (ASF) under one or more
 * # contributor license agreements.  See the NOTICE file distributed with
 * # this work for additional information regarding copyright ownership.
 * # The ASF licenses this file to You under the Apache License, Version 2.0
 * # (the "License"); you may not use this file except in compliance with
 * # the License.  You may obtain a copy of the License at
 * #
 * #     http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 * #
 */

package org.apache.process.action;

import io.kubernetes.client.Copy;
import io.kubernetes.client.Exec;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.util.Config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class QueryTestPod {
    public boolean getPodResult(String testPodName, String namespace, String testCmd, String testCodePath) throws IOException, InterruptedException, ApiException {
        System.out.println("********************query status and get result********************");
        TimeUnit.SECONDS.sleep(3);
//        ApiClient client = Config.defaultClient();
//        Configuration.setDefaultApiClient(client);
        CoreV1Api api = new CoreV1Api();
        String podStatus = api.readNamespacedPod(testPodName, namespace, null).getStatus().getPhase();
        if (podStatus == null) {
            podStatus = "Pending";
        }
        // mark if program has been executed.
        boolean isWaitingTest = true;
        while ("Pending".equals(podStatus) || "Running".equals(podStatus)) {
            TimeUnit.SECONDS.sleep(5);
            podStatus = api.readNamespacedPod(testPodName, namespace, null).getStatus().getPhase();
            if (podStatus == null) {
                podStatus = "Pending";
            }
            if (isWaitingTest) {
                System.out.printf("echo waiting for %s test done...%n", testPodName);
            } else {
                System.out.printf("current pod status is %s, waitting pod stop...%n", podStatus);
            }

            // Check if the execution of the test program has ended.
            if (isWaitingTest) {
                StringBuilder stringBuilder = null;
                try {
                    Process process = new Exec(api.getApiClient()).exec(namespace, testPodName, new String[]{"/bin/sh", "-c", "ls /root | grep testdone"}, testPodName, false, false);

                    InputStream inputStream = process.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    stringBuilder = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line);
                        stringBuilder.append(line);
                    }
                    reader.close();
                    inputStream.close();
                    process.destroy();
                } catch (Exception e) {
                    System.out.println("t");
                }

                // if the test program ends, get the result.
                if (stringBuilder != null && stringBuilder.toString().contains("testdone")) {
                    System.out.println("Test status: test done");
                    // mark that the model has been executed
                    isWaitingTest = false;
                    if (testCmd.contains("mvn")) {
                        Path path = Paths.get("test_report");
                        if (!Files.exists(path)) {
                            Files.createDirectory(path);
                        }
                        if (Files.exists(path)) {
                            // compress testdone
                            new Exec(api.getApiClient()).exec(namespace, testPodName, new String[]{"/bin/sh", "-c", "tar -zcvf testlog.tar.gz /root/testlog.txt"}, testPodName, false, false);
                            TimeUnit.SECONDS.sleep(3);
                            Copy copy = new Copy();
                            System.out.println("Copy test runlog");
                            //copy.copyFileFromPod(namespace, testPodName, testPodName,"/root/testlog.tar.gz", Paths.get("testlog.tar.gz"));
                            copy.copyDirectoryFromPod(namespace, testPodName, testPodName, "/root/testlog.tar.gz", path, true);
                            System.out.println("Copy test reports");
                            copy.copyDirectoryFromPod(namespace, testPodName, testPodName, String.format("/root/code/%s/target/surefire-reports", testCodePath), path, true);
                        }
                    }
                }
            }
        }

        System.out.println(podStatus);
        return !"Failed".equals(podStatus);
    }
}
