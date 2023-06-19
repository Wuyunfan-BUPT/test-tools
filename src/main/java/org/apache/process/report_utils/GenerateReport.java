package org.apache.process.report_utils;

import org.apache.process.report_utils.testcase.TaskResult;
import org.apache.process.report_utils.testcase.xUnitTestResultParser;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class GenerateReport {
    public boolean generateReportMarkDown(LinkedHashMap<String, Object> inputMap) {
        String xmlPath = String.format("test_report/root/code/%s/target/surefire-reports", inputMap.get("CODE_PATH").toString());
        String repoUrl = inputMap.get("REPO_URL").toString();
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
        File f=new File("result.md");//新建一个文件对象，如果不存在则创建一个该文件
        FileWriter fw;
        try {
            fw=new FileWriter(f);
            String str=res.toMarkdown(repoUrl);
            fw.write(str);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
