package org.apache.process.action;

import org.apache.process.config.Configs;
import org.apache.process.utils.SetConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import static org.apache.process.utils.GetParam.parseDeployInput;

public class DeployTest {
    @Test
    public void testStartDeploy() throws InterruptedException, IOException {
        String input="action: deploy\n" +
                "velaUsername: admin\n" +
                "velaPassword: *******\n" +
                "ask-config: \"${{ secrets.ASK_CONFIG_VIRGINA }}\"\n" +
                "waitTimes: 1200\n" +
                "namespace: nacos-12345562-23315\n" +
                "velaAppDescription: nacos-push_ci-123456@abcdefghij\n" +
                "repoName: nacos\n" +
                "helm:\n" +
                "  chart: ./helm\n" +
                "  git:\n" +
                "    branch: master\n" +
                "  repoType: git\n" +
                "  retries: 3\n" +
                "  url: \"https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-docker.git\"\n" +
                "  env: nacos-12345562-23315\n" +
                "  values:\n" +
                "    namespace: nacos-12345562-23315\n" +
                "    global:\n" +
                "      mode: cluster\n" +
                "    nacos:\n" +
                "      replicaCount: 3\n" +
                "      image:\n" +
                "        repository: wuyfeedocker/nacos-ci\n" +
                "        tag: develop-5b750342-aef4-4c1b-994d-3d9dca785dac-8\n" +
                "      storage:\n" +
                "        type: mysql\n" +
                "        db:\n" +
                "          port: 3306\n" +
                "          username: nacos\n" +
                "          password: nacos\n" +
                "          param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false\n" +
                "    service:\n" +
                "      nodePort: 30002\n" +
                "      type: ClusterIP";
        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));

        Configs.VELAUX_USERNAME = "admin";
        Configs.VELAUX_PASSWORD = "********";

        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);
        Deploy deploy = new Deploy();
        HashMap<String, Object> deployMap = parseDeployInput(input, "helm");
        Assert.assertTrue(deploy.startDeploy(deployMap));

    }

    @Test
    public void testNacosClusterStartDeploy1() throws InterruptedException, IOException {
        String input="action: deploy\n" +
                "velaUsername: admin\n" +
                "velaPassword: ********\n" +
                "ask-config: \"${{ secrets.ASK_CONFIG_VIRGINA }}\"\n" +
                "waitTimes: 1200\n" +
                "namespace: nacos-12345562-23315\n" +
                "velaAppDescription: nacos-push_ci-123456@abcdefghij\n" +
                "repoName: nacos\n" +
                "helm:\n" +
                "  chart: ./helm\n" +
                "  git:\n" +
                "    branch: master\n" +
                "  repoType: git\n" +
                "  retries: 3\n" +
                "  url: https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-k8s.git\n" +
                "  env: nacos-12345562-23315\n" +
                "  values:\n" +
                "    namespace: nacos-12345562-23315\n" +
                "    global:\n" +
                "      mode: cluster\n" +
                "    nacos:\n" +
                "      replicaCount: 3\n" +
                "      image:\n" +
                "        repository: wuyfeedocker/nacos-ci\n" +
                "        tag: develop-5b750342-aef4-4c1b-994d-3d9dca785dac-8\n" +
                "      storage:\n" +
                "        type: mysql\n" +
                "        db:\n" +
                "          port: 3306\n" +
                "          username: nacos\n" +
                "          password: nacos\n" +
                "          param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false\n" +
                "    service:\n" +
                "      nodePort: 30002\n" +
                "      type: ClusterIP";
        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));

        Configs.VELAUX_USERNAME = "admin";
        Configs.VELAUX_PASSWORD = "*********";

        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);
        Deploy deploy = new Deploy();
        HashMap<String, Object> deployMap = parseDeployInput(input, "helm");
        Assert.assertTrue(deploy.startDeploy(deployMap));

    }

    @Test
    public void testNacosStandaloneStartDeploy() throws InterruptedException, IOException {
        String input="action: deploy\n" +
                "velaUsername: admin\n" +
                "velaPassword: ********\n" +
                "ask-config: \"${{ secrets.ASK_CONFIG_VIRGINA }}\"\n" +
                "waitTimes: 1200\n" +
                "namespace: nacos-12345562-23315\n" +
                "velaAppDescription: nacos-push_ci-123456@abcdefghij\n" +
                "repoName: nacos\n" +
                "helm:\n" +
                "  chart: ./helm\n" +
                "  git:\n" +
                "    branch: master\n" +
                "  repoType: git\n" +
                "  retries: 3\n" +
                "  url: https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-k8s.git\n" +
                "  env: nacos-12345562-23315\n" +
                "  values:\n" +
                "    namespace: nacos-12345562-23315\n" +
                "    global:\n" +
                "      mode: standalone\n" +
                "    nacos:\n" +
                "      replicaCount: 1\n" +
                "      image:\n" +
                "        repository: wuyfeedocker/nacos-ci\n" +
                "        tag: develop-5b750342-aef4-4c1b-994d-3d9dca785dac-8\n" +
                "      storage:\n" +
                "        type: embedded\n" +
                "        db:\n" +
                "          port: 3306\n" +
                "          username: nacos\n" +
                "          password: nacos\n" +
                "          param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false\n" +
                "    service:\n" +
                "      nodePort: 30002\n" +
                "      type: ClusterIP";
        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));

        Configs.VELAUX_USERNAME = "admin";
        Configs.VELAUX_PASSWORD = "********";

        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);
        Deploy deploy = new Deploy();
        HashMap<String, Object> deployMap = parseDeployInput(input, "helm");
        Assert.assertTrue(deploy.startDeploy(deployMap));

    }

    @Test
    public void testRocketmqStartDeploy() throws InterruptedException, IOException {
        String input="action: deploy\n" +
                "velaUsername: admin\n" +
                "velaPassword: ********\n" +
                "ask-config: \"${{ secrets.ASK_CONFIG_VIRGINA }}\"\n" +
                "waitTimes: 1200\n" +
                "namespace: rocketmq-12345562-23315\n" +
                "velaAppDescription: rocketmq-push_ci-123456@abcdefghij\n" +
                "repoName: rocketmq\n" +
                "helm:\n" +
                "  chart: ./rocketmq-k8s-helm\n" +
                "  git:\n" +
                "    branch: master\n" +
                "  repoType: git\n" +
                "  retries: 3\n" +
                "  url: https://ghproxy.com/https://github.com/apache/rocketmq-docker.git\n" +
                "  values:\n" +
                "    nameserver:\n" +
                "      image:\n" +
                "        repository: wuyfeedocker/rocketm-ci\n" +
                "        tag: develop-3b416669-cab7-41b4-8cc8-4af851944de2-ubuntu\n" +
                "    broker:\n" +
                "      image:\n" +
                "        repository: wuyfeedocker/rocketm-ci\n" +
                "        tag: develop-3b416669-cab7-41b4-8cc8-4af851944de2-ubuntu\n" +
                "    proxy:\n" +
                "      image:\n" +
                "        repository: wuyfeedocker/rocketm-ci\n" +
                "        tag: develop-3b416669-cab7-41b4-8cc8-4af851944de2-ubuntu\n" ;

        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));

        Configs.VELAUX_USERNAME = "admin";
        Configs.VELAUX_PASSWORD = "********";

        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);
        Deploy deploy = new Deploy();
        HashMap<String, Object> deployMap = parseDeployInput(input, "helm");
        Assert.assertTrue(deploy.startDeploy(deployMap));

    }

}
