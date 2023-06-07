package org.apache.process.utils;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;


public class getKubeClient {
    private static KubernetesClient kubernetesClient;
    private getKubeClient(String kubeConfig) {
        kubernetesClient = new KubernetesClientBuilder().withConfig(kubeConfig).build();
    }
    public KubernetesClient getInstance(){
        return kubernetesClient;
    }

}
