package org.apache.process.action;

import io.kubernetes.client.openapi.ApiException;
import org.apache.process.config.Configs;
import org.apache.process.utils.SetConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static org.apache.process.utils.GetParam.yamlToMap;

public class RepoTestTest {
    @Test
    public void testRunTest() throws IOException, InterruptedException, ApiException, ExecutionException, TimeoutException {
        String input ="action: test\n" +
                "namespace: nacos-12345562-23315\n" +
                "ask-config: ${{ secrets.ASK_CONFIG_VIRGINA }}\n" +
                "API_VERSION: v1\n" +
                "KIND: Pod\n" +
                "RESTART_POLICY: \"Never\" \n" +
                "ENV:\n" +
                "  CODE: https://github.com/nacos-group/nacos-e2e.git\n" +
                "  BRANCH: main\n" +
                "  CODE_PATH: java/nacos-2X\n" +
                "  CMD: mvn clean test -B -Dnacos.client.version=2.2.3\n" +
                "  ALL_IP: null\n" +
                "CONTAINER:\n" +
                "  IMAGE: cloudnativeofalibabacloud/test-runner:v0.0.1\n" +
                "  RESOURCE_LIMITS:\n" +
                "    cpu: 8\n" +
                "    memory: 8Gi\n" +
                "  RESOURCE_REQUIRE:\n" +
                "    cpu: 8\n" +
                "    memory: 8Gi";

        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));
        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);

        LinkedHashMap<String, Object> inputMap = yamlToMap(input);
        inputMap.put("askConfig", config);

        RepoTest project = new RepoTest();
        Assert.assertTrue(project.runTest(inputMap));

    }
}
