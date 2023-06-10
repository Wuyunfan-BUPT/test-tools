package org.apache.process.utils;

import junit.framework.Assert;
import org.apache.process.model.Deploymodel;
import org.junit.Test;

import java.util.HashMap;

import static org.apache.process.utils.GetParam.parseParams;

public class GetParamTest {
    @Test
    public void testParseParams() {
        String helmvalues =
                "global:\n" +
                        "  mode: standalone\n" +
                        "nacos:\n" +
                        "  replicaCount: 1\n" +
                        "  image: \n" +
                        "    repository: wuyfeedocker/nacos-ci\n" +
                        "    tag: develop-4f26def4-ccb0-45e5-9989-874e78424bea-8\n" +
                        "  storage:\n" +
                        "    type: embedded\n" +
                        "    db:\n" +
                        "      port: 3306\n" +
                        "      username: nacos\n" +
                        "      password: nacos\n" +
                        "      param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false\n" +
                        "service:\n" +
                        "  nodePort: 30009\n" +
                        "  type: ClusterIP";
        String params = "username: nacos\n" +
                "password: nacos\n" +
                "cmd: mvn -B install\n"+
                "helm:\n" +
                "  chart: java/e2e\n"+
                "  git:\n"+
                "    branch: master\n"+
                "  repoType: git\n"+
                "  retries: 3\n"+
                "  url: https://ghproxy.com/https://github.com/apache/rocketmq-e2e.git\n"+
                "  values:\n" +
                "    global:\n" +
                "      mode: standalone\n" +
                "    nacos:\n" +
                "      replicaCount: 1\n" +
                "      image: \n" +
                "        repository: wuyfeedocker/nacos-ci\n" +
                "        tag: develop-4f26def4-ccb0-45e5-9989-874e78424bea-8\n" +
                "      storage:\n" +
                "        type: embedded\n" +
                "        db:\n" +
                "          port: 3306\n" +
                "          username: nacos\n" +
                "          password: nacos\n" +
                "          param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false\n" +
                "    service:\n" +
                "      nodePort: 30009\n" +
                "      type: ClusterIP";
        String target = "helm:";
        HashMap<String, String> mapp = parseParams(params, target);
        for(String key: mapp.keySet()){
            System.out.println(key+": "+ mapp.get(key));
        }
        Assert.assertEquals(mapp.get("username"), "nacos");
        Assert.assertEquals(mapp.get("password"), "nacos");
        Assert.assertEquals(mapp.get("cmd"), "mvn -B install");
        Assert.assertEquals(Deploymodel.generateComponentProperties(helmvalues,"java/e2e","master", "https://ghproxy.com/https://github.com/apache/rocketmq-e2e.git").length(), mapp.get("helm:").length());
        System.out.println(mapp.get("helm:"));
    }
}
