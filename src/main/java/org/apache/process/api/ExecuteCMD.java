package org.apache.process.api;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import io.fabric8.kubernetes.client.dsl.ExecListener;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ExecuteCMD implements AutoCloseable{
    private final KubernetesClient client;
    @SuppressWarnings("java:S106")
    public ExecuteCMD(String config) {
        this.client = new KubernetesClientBuilder().withConfig(config).build();
    }
    @SneakyThrows
    public String execCommandOnPod(String podName, String namespace, String... cmd) throws ExecutionException, InterruptedException, TimeoutException {
        Pod pod = client.pods().inNamespace(namespace).withName(podName).get();
        CompletableFuture<String> data = new CompletableFuture<>();
        try (ExecWatch execWatch = execCmd(pod, data, cmd)) {
            return data.get(20, TimeUnit.SECONDS);
        }

    }
    @Override
    public void close() {
        client.close();
    }

    private ExecWatch execCmd(Pod pod, CompletableFuture<String> data, String... command) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        return client.pods()
                .inNamespace(pod.getMetadata().getNamespace())
                .withName(pod.getMetadata().getName())
                .writingOutput(baos)
                .writingError(baos)
                .usingListener(new SimpleListener(data, baos))
                .exec(command);
    }

    static class SimpleListener implements ExecListener {

        private CompletableFuture<String> data;
        private ByteArrayOutputStream baos;

        public SimpleListener(CompletableFuture<String> data, ByteArrayOutputStream baos) {
            this.data = data;
            this.baos = baos;
        }

        @Override
        public void onOpen() {
            System.out.println("checking testdone... ");
        }

        @Override
        public void onFailure(Throwable t, Response failureResponse) {
            System.err.println(t.getMessage());
            data.completeExceptionally(t);
        }

        @Override
        public void onClose(int code, String reason) {
            data.complete(baos.toString());
        }
    }
}
