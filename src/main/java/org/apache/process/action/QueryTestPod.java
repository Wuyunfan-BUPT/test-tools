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
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import org.apache.process.api.ExecuteCMD;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class QueryTestPod {
    /**
     * get dictionary and file about test result from pod.
     * @param config ask config.
     * @param testPodName test pod name.
     * @param namespace pod namespace.
     * @param testCodePath test code path.
     * @return boolean.
     * @throws IOException io exception.
     * @throws InterruptedException interrupt exception.
     */
    public boolean getPodResult(String config, String testPodName, String namespace, String testCodePath) throws IOException, InterruptedException {
        System.out.println("********************query status and get result********************");
        TimeUnit.SECONDS.sleep(3);

        KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
        String podStatus = null;
        try{
            podStatus = client.pods().inNamespace(namespace).withName(testPodName).get().getStatus().getPhase();
        }catch (Exception ignored) {
            System.out.println("podStatus set Pending..");
        }

        if (podStatus == null) {
            podStatus = "Pending";
        }
        // mark if program has been executed.
        boolean isWaitingTest = true;
        while ("Pending".equals(podStatus) || "Running".equals(podStatus) || isWaitingTest) {
            TimeUnit.SECONDS.sleep(5);
            try{
                podStatus = client.pods().inNamespace(namespace).withName(testPodName).get().getStatus().getPhase();
            }catch(Exception e){
                System.out.println("Query pod fail! retry again...");
            }

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
                    cmdOutput = executeCMD.execCommandOnPod(testPodName, namespace, "/bin/bash", "-c", "ls /root | grep testdone\n");

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("query error! continue to query...");
                }
//                try{
//                    String[] command = {"/bin/bash", "-c", "ls /root | grep testdone"};
//                    ExecWatch watch = client.pods().inNamespace(namespace).withName(testPodName)
//                            .inContainer(testPodName).redirectingInput().redirectingOutput()
//                            .redirectingError().exec(command);
//                    cmdOutput = watch.getOutput().toString();
//                    System.out.println(cmdOutput);
//                    watch.close();
//
//                }catch (Exception e) {
//                    e.printStackTrace();
//                    System.out.println("query error! continue to query...");
//                }

                // if the test program ends, get the result.
                if (cmdOutput != null && cmdOutput.contains("testdone")) {
                    System.out.println("test done !");

                    isWaitingTest = false;

                    Path filePath = Paths.get("testlog.txt");
                    if (!Files.exists(filePath)) {
                        Files.createFile(filePath);
                    }
                    int downloaTimes = 3;
                    boolean isStop = true;
                    while(isStop && downloaTimes>0) {
                            isStop = !downloadFile(config, namespace, testPodName, testPodName, "/root/testlog.txt", filePath);
                            downloaTimes--;
                    }


                    Path dirPath = Paths.get("test_report");
                    if (!Files.exists(dirPath)) {
                        Files.createDirectory(dirPath);
                    }
                    downloaTimes = 3;
                    isStop = true;
                    while(isStop && downloaTimes>0) {

                            isStop = !downloadDir(config, namespace, testPodName, testPodName, String.format("/root/code/%s/target/surefire-reports", testCodePath), dirPath);
                            //isStop = false;
                            //System.out.println("download surefire-reports fail! retry...");
                            downloaTimes--;
                    }
                }
            }
        }

        System.out.println("Test status: " + podStatus);
        return !"Failed".equals(podStatus);
    }

    /**
     * download file from pod.
     * @param config ask config.
     * @param namespace pod namespace.
     * @param podName pod name.
     * @param containerName pod's container name.
     * @param srcPath file path in pod.
     * @param targetPath target file path.
     */
    public boolean downloadFile(String config, String namespace, String podName, String containerName, String srcPath, Path targetPath) {
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).file(srcPath).copy(targetPath);
            System.out.printf("File(%s) copied successfully!%n", srcPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Fail to get %s!%n", srcPath);
            return false;
        }
    }

    /**
     * download dictionary from pod.
     * @param config ask config.
     * @param namespace pod namespace.
     * @param podName pod name.
     * @param containerName pod's container name.
     * @param srcPath dictionary path in pod
     * @param tarPath target dictionary path.
     */
    public boolean downloadDir(String config, String namespace, String podName, String containerName, String srcPath, Path tarPath) {
        try (KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build()) {
            client.pods().inNamespace(namespace).withName(podName).inContainer(containerName).dir(srcPath).copy(tarPath);
            System.out.printf("Directory(%s) copied successfully!%n", srcPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.printf("Fail to get %s!%n", srcPath);
            return false;
        }
    }

}
