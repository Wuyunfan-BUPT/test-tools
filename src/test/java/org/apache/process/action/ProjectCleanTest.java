package org.apache.process.action;

import org.apache.process.config.Configs;
import org.apache.process.utils.SetConfig;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static org.apache.process.utils.GetParam.yamlToMap;

public class ProjectCleanTest {
    @Test
    public void testClean() throws IOException, InterruptedException {
        String input = "action: clean\n" +
                "namespace: nacos-12345562-23315\n" +
                "ask-config: ${{ secrets.ASK_CONFIG_VIRGINA }}\n" +
                "velaUsername: admin\n" +
                "velaPassword: ********";
        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));
        Configs.VELAUX_USERNAME = "admin";
        Configs.VELAUX_PASSWORD = "********";

        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);

        LinkedHashMap<String, Object> inputMap = yamlToMap(input);
        EnvClean envClean = new EnvClean();
        Assert.assertTrue(envClean.clean(inputMap));
    }


    @Test
    public void testRocketmqClean() throws IOException, InterruptedException {
        String input = "action: clean\n" +
                "namespace: rocketmq-12345562-23315\n" +
                "ask-config: ${{ secrets.ASK_CONFIG_VIRGINA }}\n" +
                "velaUsername: admin\n" +
                "velaPassword: ********";
        String usrHome = System.getProperty("user.home");
        String fileName = String.format("%s/.kube/config", usrHome);
        String config = new String(Files.readAllBytes(Paths.get(fileName)));
        Configs.VELAUX_USERNAME = "admin";
        Configs.VELAUX_PASSWORD = "********";

        SetConfig setConfig = new SetConfig();
        String kubeConfigPath = setConfig.setConfig(config);
        setConfig.setKubeClientConfig(kubeConfigPath);

        new PortForward().startPortForward(Configs.VELA_NAMESPACE, Configs.VELA_POD_LABELS, Configs.PORT_FROWARD, config);

        LinkedHashMap<String, Object> inputMap = yamlToMap(input);
        EnvClean envClean = new EnvClean();
        Assert.assertTrue(envClean.clean(inputMap));
    }
}
