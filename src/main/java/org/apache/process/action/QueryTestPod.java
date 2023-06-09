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

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.apache.process.api.ExecuteCMD;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class QueryTestPod {
    public boolean getPodResult(String config, String testPodName, String namespace, String testCodePath) throws IOException, InterruptedException {
        System.out.println("********************query status and get result********************");
        TimeUnit.SECONDS.sleep(3);

        KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();

        String podStatus = client.pods().inNamespace(namespace).withName(testPodName).get().getStatus().getPhase();
        if (podStatus == null) {
            podStatus = "Pending";
        }
        // mark if program has been executed.
        boolean isWaitingTest = true;
        while ("Pending".equals(podStatus) || "Running".equals(podStatus)) {
            TimeUnit.SECONDS.sleep(5);
            podStatus = client.pods().inNamespace(namespace).withName(testPodName).get().getStatus().getPhase();
            if (podStatus == null) {
                podStatus = "Pending";
            }
            if (isWaitingTest) {
                System.out.printf("waiting for %s test done...%n", testPodName);
            } else {
                System.out.printf("current pod status is %s, waitting pod stop...%n", podStatus);
            }

            // Check if the execution of the test program has ended.
            if (isWaitingTest) {
                String cmdOutput = null;
                try (ExecuteCMD executeCMD = new ExecuteCMD(config)) {
                    cmdOutput = executeCMD.execCommandOnPod(testPodName, namespace, "/bin/sh", "-c", "ls /root | grep testdone");
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("query error! continue to query...");
                }
                // if the test program ends, get the result.
                if (cmdOutput != null && cmdOutput.contains("testdone")) {
                    System.out.println("test done !");

                    isWaitingTest = false;

                    Path filePath = Paths.get("testlog.txt");
                    if (!Files.exists(filePath)) {
                        Files.createFile(filePath);
                    }
                    downloadFile(config, namespace, testPodName, testPodName, "/root/testlog.txt", filePath);

                    Path dirPath = Paths.get("test_report");
                    if (!Files.exists(dirPath)) {
                        Files.createDirectory(dirPath);
                    }
                    downloadDir(config, namespace, testPodName, testPodName, String.format("/root/code/%s/target/surefire-reports", testCodePath), dirPath);
                }
            }
        }

        System.out.println("Test status: " + podStatus);
        return !"Failed".equals(podStatus);
    }

    public void downloadFile(String config, String namespace, String podName, String containerName, String srcPath, Path targetPath) {
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).file(srcPath).copy(targetPath);
            System.out.printf("File(%s) copied successfully!%n", srcPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Fail to get %s!%n", srcPath);
        }
    }

    public void downloadDir(String config, String namespace, String podName, String containerName, String srcPath, Path tarPath) {
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).dir(srcPath).copy(tarPath);
            System.out.printf("Directory(%s) copied successfully!%n", srcPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Fail to get %s!%n", srcPath);
        }
    }

}
