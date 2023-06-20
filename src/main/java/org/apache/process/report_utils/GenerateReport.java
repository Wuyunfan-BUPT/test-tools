package org.apache.process.report_utils;

import org.apache.process.report_utils.testcase.TaskResult;
import org.apache.process.report_utils.testcase.xUnitTestResultParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GenerateReport {
    public boolean generateReportMarkDown(LinkedHashMap<String, Object> inputMap) {
        LinkedHashMap<String, Object> envMap = (LinkedHashMap)inputMap.get("ENV");
        String repoUrl = splitHttps(envMap.get("CODE").toString())+"/tree/"+envMap.get("BRANCH").toString()+"/"+envMap.get("CODE_PATH").toString()+"/src/test/java";
        String xmlPath = String.format("test_report/root/code/%s/target/surefire-reports", envMap.get("CODE_PATH").toString());
        List<File> fileList = new ArrayList<>();
        File file = new File(xmlPath);
        String[] files = file.list((dir, name) -> {
            // TODO Auto-generated method stub
            return name.endsWith(".xml");
        });
        for (String s : files) {
            fileList.add(new File(xmlPath + "/" + s));
            System.out.println(s);
        }
        xUnitTestResultParser parser = new xUnitTestResultParser();
        TaskResult res = parser.parseTestResult(fileList);
        File f=new File("result.md");
        FileWriter fw;
        try {
            fw=new FileWriter(f);
            String str=res.toMarkdown(repoUrl);
            fw.write(str);
            fw.close();
            System.out.println("Generate report success!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Fail to generate report!");
            return false;
        }
        return true;
    }

    public String splitHttps(String url){
        String[] httpUrl = url.split("https://");
        if(httpUrl.length == 1) {
            return httpUrl[0];
        } else{
            return "https://" +httpUrl[httpUrl.length-1];
        }
    }
}
