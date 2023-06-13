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

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class RepoTest {

    public boolean runTest(LinkedHashMap<String, Object> inputMap) throws ApiException, IOException, InterruptedException, ExecutionException, TimeoutException {
        System.out.println("**************E2E TEST...***************");
        CoreV1Api api = new CoreV1Api();

        String namespace = inputMap.get("namespace").toString();
        String testPodName = "test-" + namespace + "-"+ new Random().nextInt(100000);
        System.out.printf("** namespace: %s **%n", namespace);
        // create a pod
        V1Pod pod_template = new V1Pod();
        pod_template.setApiVersion(inputMap.get("API_VERSION").toString());
        pod_template.setKind(inputMap.get("KIND").toString());
        // set pod: metadata
        pod_template.setMetadata(new V1ObjectMeta().name(testPodName).namespace(namespace));//
        // set pod: spec
        V1PodSpec pod_template_spec = new V1PodSpec();
        pod_template_spec.setRestartPolicy(inputMap.get("RESTART_POLICY").toString());
        // set pod spec: container
        V1Container container = new V1Container();
        System.out.println("*******************" + testPodName + "*******************");


        // ***********set container***********
        // set pod spec container: name and images
        LinkedHashMap<String, Object>  containerMap = (LinkedHashMap)inputMap.get("CONTAINER");
        container.setName(testPodName);
        container.setImage(containerMap.get("IMAGE").toString());
        // set pod spec container: resource
        LinkedHashMap<String, Object>  limitsResourceMap = (LinkedHashMap)containerMap.get("RESOURCE_LIMITS");
        HashMap<String, Quantity> resourcesLimits = new HashMap<>();
        for(String limitKey: limitsResourceMap.keySet()){
            resourcesLimits.put(limitKey, new Quantity(limitsResourceMap.get(limitKey).toString()));
        }
        LinkedHashMap<String, Object>  requestResourceMap = (LinkedHashMap)containerMap.get("RESOURCE_REQUIRE");
        HashMap<String, Quantity> resourceRequests = new HashMap<>();
        for(String requestKey: requestResourceMap.keySet()){
            resourcesLimits.put(requestKey, new Quantity(requestResourceMap.get(requestKey).toString()));
        }
        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        v1ResourceRequirements.setLimits(resourcesLimits);
        v1ResourceRequirements.setRequests(resourceRequests);
        container.setResources(v1ResourceRequirements);

        // set V container: env elements
        LinkedHashMap<String, Object> envMap = (LinkedHashMap)inputMap.get("ENV");
        if(envMap.get("ALL_IP")==null || "null".equals(envMap.get("ALL_IP"))){
            /* get all IP */
            V1PodList pods = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
            StringBuilder allIP = new StringBuilder();
            System.out.println("come in");
            for (V1Pod pod : pods.getItems()) {
                System.out.println("------------");
                allIP.append(pod.getMetadata().getName()).append(":").append(pod.getStatus().getPodIP()).append(",");
            }
            envMap.put("ALL_IP", allIP.substring(0, allIP.length()-1));
        }

        for(String envKey: envMap.keySet()){
            V1EnvVar v1EnvVar = new V1EnvVar();
            v1EnvVar.setName(envKey);
            v1EnvVar.setValue(envMap.get(envKey).toString());
            container.addEnvItem(v1EnvVar);
        }

        // add container to spec
        pod_template_spec.addContainersItem(container);

        // add spec to pod
        pod_template.setSpec(pod_template_spec);

        // create pod
        api.createNamespacedPod(namespace, pod_template, null, null, null, null);
        return new QueryTestPod().getPodResult(inputMap.get("askConfig").toString(), testPodName, namespace, envMap.get("CODE_PATH").toString());
    }
}
