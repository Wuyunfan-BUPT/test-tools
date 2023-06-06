# test-tools
This project is used for repository testing, including deployment, testing, cleaning and other processes, currently supports rocketmq and nacos.

## Usage
|     params     | required |             mean              |     values      |                                                                                default                                                                                 |
|:--------------:|  :----:  |:-----------------------------:|:---------------:|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------:|
|    testRepo    | true |        test Resonsity         | nacos, rocketmq |                                                                                  null                                                                                  |
|     action     | false |    run in local / cluster     | local / cluster |                                                       cluster(not support this param currently， retain keyword)                                                        |
|    version     | false |         mark workflow         |     string      |                                                                                  null                                                                                  |
|    jobIndex    | false |       github job index        |     number      |                                                                                   0                                                                                    |
|   askConfig    | true |      ask cluster config       |     string      |                                                                                  null                                                                                  |
| velauxUserName | true |        velaUX username        |     string      |                                                                                  null                                                                                  |
| velauxPassword | true |        velaUX password        |     string      |                                                                                                                         null                                           |
|    chartGit    | false |     helm chart repository     |       url       |                                                                                   rocketmq: https://ghproxy.com/https://github.com/apache/rocketmq-docker.git <br/> nacos: https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-docker.git |
|   chartPath    | false | helm chart path in repository |      path       |                                                                                                                                        rocketmq: ./rocketmq-k8s-helm <br/> nacos: ./helm                                                        |
|  chartBranch   | false |       helm chart branch       |     branch      |                                                                                                                                                               master                                                                             |
|   helmValue    | true |       helm chart values       |   yaml struct   |                                                                                                                                                               ...                                                                               |
|  testCodeGit   | false |     test code repository      |       url       |                                                                                                         rocketmq: https://github.com/apache/rocketmq-e2e.git <br/> nacos:  https://github.com/nacos-group/nacos-e2e.git                         |
|  testCodePath  | false | test code path in repository  |      path       |                                                                                                                                          rocketmq: java/e2e <br/> nacos:  java/nacos-2X                                                         |
|  testCmdBase   | false |   execute test code command   |     command     |                                                                                                                                       rocketmq: mvn -B test <br/> nacos:   mvn clean test -B                                                     |

## start project
### by java jar
```agsl
cd test-tools
mvn clean install
mv /target/rocketmq-test-tools-1.0-SNAPSHOT-jar-*.jar ./rocketmq-test-tools.jar
# quick start run
jar -jar rocketmq-test-tools.jar -testRepo=nacos -askConfig=${your ask config}  -velauxUsername=${your velaux username}  -velauxPassword=${your velaux password} 
# or
jar -jar rocketmq-test-tools.jar -testRepo=nacos -version=123456 -jobIndex=1 -askConfig=${your ask config}  -velauxUsername=${your velaux username}  -velauxPassword=${your velaux password} -chartGit=https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-docker.git -chartPath=./helm chartBranch=master helmValue=${your helmValue} -testCodeGit=https://github.com/nacos-group/nacos-e2e.git -testCodePath=java/nacos-2X -testCmdBase="mvn clean test -B" 
```
### by docker images
```
# build docker images
docker build -t test-tools
# quick start run
docker run -it test-tools -testRepo=nacos -askConfig=${your ask config}  -velauxUsername=${your velaux username}  -velauxPassword=${your velaux password} 
# or
docker run -it test-tools -testRepo=nacos -version=123456 -jobIndex=1 -askConfig=${your ask config}  -velauxUsername=${your velaux username}  -velauxPassword=${your velaux password} -chartGit=https://ghproxy.com/https://github.com/Wuyunfan-BUPT/nacos-docker.git -chartPath=./helm chartBranch=master helmValue=${your helmValue} -testCodeGit=https://github.com/nacos-group/nacos-e2e.git -testCodePath=java/nacos-2X -testCmdBase="mvn clean test -B" 
```
#### helmvalues example
##### rocketmq helm values example
```agsl
nameserver:
  image:
    repository: apache/rocketmq-ci
    tag: develop-7be7f477-ddcb-45d0-910b-92a213f7a37c-ubuntu
broker:
  image:
    repository: apache/rocketmq-ci
    tag: develop-7be7f477-ddcb-45d0-910b-92a213f7a37c-ubuntu
proxy:
  image:
    repository: apache/rocketmq-ci
    tag: develop-7be7f477-ddcb-45d0-910b-92a213f7a37c-ubuntu
```
##### nacos helm values example
```agsl
global:
  mode: cluster
nacos:
  replicaCount: 3
  image:
    repository: wuyfeedocker/nacos-ci
    tag: develop-4f26def4-ccb0-45e5-9989-874e78424bea-8
  storage:
    type: mysql
    db:
      port: 3306
      username: nacos
      password: nacos
      param: characterEncoding=utf8&connectTimeout=1000&socketTimeout=3000&autoReconnect=true&useSSL=false
service:
  nodePort: 30009
  type: ClusterIP
```






