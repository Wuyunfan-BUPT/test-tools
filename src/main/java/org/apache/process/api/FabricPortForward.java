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

package org.apache.process.api;


import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.LocalPortForward;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import org.apache.process.config.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

public class FabricPortForward {
        private static final Logger logger = LoggerFactory.getLogger(FabricPortForward.class);
        public static void main(String[] args) throws InterruptedException {
            String namespace = "vela-system";
            String podLabels = "addon-velaux";
            int localPort = 9082;

            new Thread(
                    new Runnable() {
                        @Override
                        public void run() {
                            try {
                                System.out.println();
                                //podPortForward(namespace, podLabels, localPort);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }).start();
            int count = 5;
            while(count>0){
                count--;
                TimeUnit.SECONDS.sleep(5);
                System.out.println("port forward......");
            }
            System.out.println("end");
            System.exit(0);
        }
        public void podPortForward(String namespace, String podLabels, int localPort){

            try (KubernetesClient client = new KubernetesClientBuilder().build()) {
                System.out.printf("Using namespace: %s %n", namespace);
                System.out.printf("Using podLabels: %s %n", podLabels);
                PodList pode = client.pods().inNamespace(namespace).list();
                for(Pod p:pode.getItems()){
                    String labels = p.getMetadata().getLabels().get("app.oam.dev/name");
                    System.out.printf("labels: %s %n", labels);
                    if (podLabels.equals(labels)) {
                        System.out.printf("find labels");
                        int containerPort = p.getSpec().getContainers().get(0).getPorts().get(0).getContainerPort();
                        client.pods().inNamespace(namespace).withName(p.getMetadata().getName()).waitUntilReady(10, TimeUnit.SECONDS);

                        InetAddress inetAddress = InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 });
                        LocalPortForward portForward;
                        new Thread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            portForward = client.pods().inNamespace(namespace).withName(p.getMetadata().getName()).portForward(containerPort,
                                                    inetAddress, localPort);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                           }
                                }).start();
                        TimeUnit.SECONDS.sleep(5);

                        System.out.println("Checking forwarded port......");
                        final ResponseBody responseBody = new OkHttpClient()
                                .newCall(new Request.Builder().get().url("http://127.0.0.1:" + portForward.getLocalPort()).build()).execute()
                                .body();
                        TimeUnit.MINUTES.sleep(Configs.MAX_RUN_TIME);
                        System.out.println("Closing forwarded port");
                        portForward.close();
                    }
                }
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

