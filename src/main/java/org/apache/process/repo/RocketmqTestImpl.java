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

package org.apache.process.repo;

import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.*;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.exception.CopyNotSupportedException;
import org.apache.process.action.Deploy;
import org.apache.process.action.ProjectClean;
import org.apache.process.action.QueryTestPod;
import org.apache.process.config.Configs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class RocketmqTestImpl implements RepoTest {
    public static HashMap<String, String> contextMap;

    public RocketmqTestImpl(HashMap<String, String> map) {
        contextMap = map;
    }


    @Override
    public boolean deploy() throws IOException, InterruptedException, ApiException {
        String appName = contextMap.get("env");
        String alias = contextMap.get("env");
        String description = contextMap.getOrDefault("velaAppDescription", null);
        String repoName = contextMap.getOrDefault("repoName","rocketmq");
        String chartPath = contextMap.getOrDefault("chartPath","./rocketmq-k8s-helm");
        String chartBranch = contextMap.getOrDefault("chartBranch","master");
        String chartGit = contextMap.getOrDefault("chartGit", "https://ghproxy.com/https://github.com/apache/rocketmq-docker.git");
        String env = contextMap.get("env");
        String helmValue = contextMap.getOrDefault("helmValue", "nameserver:\n" +
                "  image:\n" +
                "    repository: apache/rocketmq-ci\n" +
                "    tag: develop-7be7f477-ddcb-45d0-910b-92a213f7a37c-ubuntu\n" +
                "broker:\n" +
                "  image:\n" +
                "    repository: apache/rocketmq-ci\n" +
                "    tag: develop-7be7f477-ddcb-45d0-910b-92a213f7a37c-ubuntu\n" +
                "proxy:\n" +
                "  image:\n" +
                "    repository: apache/rocketmq-ci\n" +
                "    tag: develop-7be7f477-ddcb-45d0-910b-92a213f7a37c-ubuntu\n");

        Deploy deploy = new Deploy();
        return deploy.runDeployTestTools(appName,alias,description,
                repoName,chartPath,chartBranch,chartGit,
                Configs.PROJECT_NAME,env, helmValue);
    }
    @Override
    public boolean testRepo() throws IOException, ApiException, InterruptedException, CopyNotSupportedException {
        System.out.println("**************E2E TEST...***************");
        String namespace = contextMap.get("env");
        String testCodeGit = contextMap.getOrDefault("testCodeGit", "https://github.com/apache/rocketmq-e2e.git");
        String testCodeBranch = contextMap.getOrDefault("testCodeBranch","master");
        String testCodePath = contextMap.getOrDefault("testCodePath", "java/e2e");
        String testCmdBase = contextMap.getOrDefault("testCmdBase","mvn -B test");

        ApiClient client = Config.defaultClient();
        Configuration.setDefaultApiClient(client);

        CoreV1Api api = new CoreV1Api();

        V1PodList pods = api.listNamespacedPod(namespace, null, null, null, null, null, null, null, null, null, null);
        StringBuilder allIP = new StringBuilder();
        for (V1Pod pod : pods.getItems()) {
            String podName = pod.getMetadata().getName();
            if (podName.contains("test-" + namespace)) {
                continue;
            }
            allIP.append(podName).append(":").append(pod.getStatus().getPodIP()).append(",");
        }

        System.out.println("**************create pod***************");
        /* create pod */
        HashMap<String, Quantity> resourcesLimits = new HashMap<>();
        resourcesLimits.put("cpu", new Quantity("8"));
        resourcesLimits.put("memory", new Quantity("8Gi"));
        HashMap<String, Quantity> resourceRequests = new HashMap<>();
        resourceRequests.put("cpu", new Quantity("8"));
        resourceRequests.put("memory", new Quantity("8Gi"));

        String testPodName = "test-" + namespace + new Random().nextInt(100000);
        // create a pod
        V1Pod pod_template = new V1Pod();
        pod_template.setApiVersion("v1");
        pod_template.setKind("Pod");
        // set pod: metadata
        pod_template.setMetadata(new V1ObjectMeta().name(testPodName).namespace(namespace));//
        // set pod: spec
        V1PodSpec pod_template_spec = new V1PodSpec();
        pod_template_spec.setRestartPolicy("Never");
        // set pod spec: container
        V1Container container = new V1Container();
        System.out.println("**********************" + testPodName + "****************");


        // ***********set container***********
        // set pod spec container: name and images
        container.setName(testPodName);
        container.setImage("cloudnativeofalibabacloud/test-runner:v0.0.1");
        // set pod spec container: resource
        V1ResourceRequirements v1ResourceRequirements = new V1ResourceRequirements();
        v1ResourceRequirements.setLimits(resourcesLimits);
        v1ResourceRequirements.setRequests(resourceRequests);
        container.setResources(v1ResourceRequirements);

        // set V container: env elements
        V1EnvVar v1EnvVar1 = new V1EnvVar();
        v1EnvVar1.setName("CODE");
        v1EnvVar1.setValue(testCodeGit);
        container.addEnvItem(v1EnvVar1);

        V1EnvVar v1EnvVar2 = new V1EnvVar();
        v1EnvVar2.setName("BRANCH");
        v1EnvVar2.setValue(testCodeBranch);
        container.addEnvItem(v1EnvVar2);

        V1EnvVar v1EnvVar3 = new V1EnvVar();
        v1EnvVar3.setName("CODE_PATH");
        v1EnvVar3.setValue(testCodePath);
        container.addEnvItem(v1EnvVar3);

        String testCmd = testCmdBase;
        if (testCmd.contains("mvn")) {
            testCmd = testCmdBase + " -DALL_IP=" + allIP;
        }
        V1EnvVar v1EnvVar4 = new V1EnvVar();
        v1EnvVar4.setName("CMD");
        v1EnvVar4.setValue(testCmd);
        container.addEnvItem(v1EnvVar4);

        V1EnvVar v1EnvVar5 = new V1EnvVar();
        v1EnvVar5.setName("ALL_IP");
        v1EnvVar5.setValue(allIP.substring(0, allIP.length() - 1));
        container.addEnvItem(v1EnvVar5);
        // add container to spec
        pod_template_spec.addContainersItem(container);

        // add spec to pod
        pod_template.setSpec(pod_template_spec);

        // create pod
        V1Pod createdPod = api.createNamespacedPod(namespace, pod_template, null, null, null, null);
        System.out.println(createdPod.getStatus().getPhase());

        // query and wait result.
        return new QueryTestPod().getPodResult(testPodName, namespace, testCmd, testCodePath);


    }
    @Override
    public void clean() throws IOException {
        new ProjectClean().clean(contextMap.get("env"), contextMap.get("env"));
    }
}
