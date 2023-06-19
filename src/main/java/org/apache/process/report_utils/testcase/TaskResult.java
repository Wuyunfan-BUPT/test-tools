package org.apache.process.report_utils.testcase;

import org.apache.process.report_utils.MarkdownBuilder;
import org.apache.process.report_utils.MarkdownLinkBuilder;
import org.apache.process.report_utils.MarkdownTableBuilder;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wangtong.wt on 2017/3/20.
 *
 * @author wangtong.wt
 * @date 2017/03/20
 */
@Data
public class TaskResult {

    /**
     * 用例执行时间
     */
    protected double costTime;

    private Map<String, CaseResult> successCaseMap = new HashMap<>();
    private Map<String, CaseResult> failureCaseMap = new HashMap<>();
    private Map<String, CaseResult> errorCaseMap = new HashMap<>();
    private Map<String, CaseResult> skipCaseMap = new HashMap<>();
    private Map<String, String> caseTypeMap = new HashMap<>();

    public TaskResult() {}

    public void addCostTime(double time) {
        this.costTime = this.costTime + time;
    }

    public void addCase(CaseResult caseResult) {
        String type = caseResult.getResult();
        String caseName = caseResult.getClassName() + "#" + caseResult.getMethodName();

        if (!(StringUtils.equals(type, CaseResult.CASE_RESULT_SUCCESS)
                || StringUtils.equals(type, CaseResult.CASE_RESULT_FAILURE)
                || StringUtils.equals(type, CaseResult.CASE_RESULT_ERROR)
                || StringUtils.equals(type, CaseResult.CASE_RESULT_SKIPPED))) {
            return;
        }

        //判断历史运行的Case是否包含当前运行的Case
        // 如果包含，则判断历史运行的Case是成功，还是失败，如果成功，则忽略，如果失败，则覆盖
        if (caseTypeMap.containsKey(caseName)) {
            if (StringUtils.equals(caseTypeMap.get(caseName), CaseResult.CASE_RESULT_SUCCESS)) {
                return;
            }
            if (StringUtils.equals(caseTypeMap.get(caseName), CaseResult.CASE_RESULT_FAILURE)) {
                if (StringUtils.equals(type, CaseResult.CASE_RESULT_SUCCESS)) {
                    failureCaseMap.remove(caseName);
                    successCaseMap.put(caseName, caseResult);
                } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_FAILURE)) {
                    failureCaseMap.put(caseName, caseResult);
                } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_ERROR)) {
                    failureCaseMap.remove(caseName);
                    errorCaseMap.put(caseName, caseResult);
                }
            } else if (StringUtils.equals(caseTypeMap.get(caseName), CaseResult.CASE_RESULT_ERROR)) {
                if (StringUtils.equals(type, CaseResult.CASE_RESULT_SUCCESS)) {
                    errorCaseMap.remove(caseName);
                    successCaseMap.put(caseName, caseResult);
                } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_FAILURE)) {
                    errorCaseMap.remove(caseName);
                    failureCaseMap.put(caseName, caseResult);
                } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_ERROR)) {
                    errorCaseMap.put(caseName, caseResult);
                }
            }
            caseTypeMap.put(caseName, type);
        } else {
            if (StringUtils.equals(type, CaseResult.CASE_RESULT_SUCCESS)) {
                successCaseMap.put(caseName, caseResult);
            } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_FAILURE)) {
                failureCaseMap.put(caseName, caseResult);
            } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_ERROR)) {
                errorCaseMap.put(caseName, caseResult);
            } else if (StringUtils.equals(type, CaseResult.CASE_RESULT_SKIPPED)) {
                skipCaseMap.put(caseName, caseResult);
            }
            caseTypeMap.put(caseName, type);
        }
    }

    public String toMarkdown(String repoUrl) {
        MarkdownBuilder builder = MarkdownBuilder.builder();

        builder.addHeader("Test Cases Info", 2);

        MarkdownTableBuilder tableBuilder = MarkdownTableBuilder.builder();
        tableBuilder.addHead("Total", "Success", "Failure", "Error", "Skipped");
        tableBuilder.addRow(getTotalCount(), getSuccessCount(), getFailureCount(),
                getErrorCount(), getSkipCount());

        builder.addTable(tableBuilder);

        builder.addHeader(":x: Failed Case Detail", 3);

        List<CaseResult> allBadCase = new ArrayList<>(failureCaseMap.values());
        allBadCase.addAll(errorCaseMap.values());

        for (CaseResult badCase : allBadCase) {
            MarkdownLinkBuilder linkBuilder = MarkdownLinkBuilder.builder();

            linkBuilder.setLink(badCase.getClassName() + "." + badCase.getMethodName(),
                    repoUrl+"/"+badCase.getClassName().replace(".", "/") +".java#L"+getLineNumber(badCase.getDetailInfo()).split(":")[1]);
            System.out.println(badCase.getDetailInfo());
            System.out.println(getLineNumber(badCase.getDetailInfo()).split(":")[1]);


            builder.addHeader("Name: " + linkBuilder.build() +
                    " Time: " + badCase.getTime() + "s", 4);

            MarkdownBuilder exceptionBuilder = MarkdownBuilder.builder();
            exceptionBuilder.newLine();
            exceptionBuilder.addBoldText("Exception").newLine();
            exceptionBuilder.addText(badCase.getDetailInfo()).newLine();
            exceptionBuilder.addBoldText("System out info").newLine();
            exceptionBuilder.addText(badCase.getSysoutLog()).newLine();

            builder.addCollapse("Exception Detail", exceptionBuilder.build());
        }

        builder.addHeader(":white_check_mark: Success Cases", 3);
        for (CaseResult successCase : successCaseMap.values()) {
            MarkdownLinkBuilder linkBuilder = MarkdownLinkBuilder.builder();
            linkBuilder.setLink(successCase.getClassName() + "." + successCase.getMethodName(),
                    repoUrl);

            builder.addHeader("Name: " + linkBuilder.build() +
                    " Time: " + successCase.getTime() + "s", 4);
        }

        builder.addHeader(":next_track_button: Skipped Cases", 3);
        for (CaseResult skippedCase : skipCaseMap.values()) {
            MarkdownLinkBuilder linkBuilder = MarkdownLinkBuilder.builder();
            linkBuilder.setLink(skippedCase.getClassName() + "." + skippedCase.getMethodName(),
                    repoUrl);

            builder.addHeader("Name: " + linkBuilder.build() +
                    " Time: " + skippedCase.getTime() + "s", 4);
        }

        return builder.build();
    }

    public int getSuccessCount() {
        return successCaseMap.size();
    }

    public int getFailureCount() {
        return failureCaseMap.size();
    }

    public int getErrorCount() {
        return errorCaseMap.size();
    }
    public int getSkipCount() {
        return skipCaseMap.size();
    }

    public int getTotalCount() {
        return caseTypeMap.size();
    }

    public static String getLineNumber(String str){
        List<String> strList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?<=\\()[^\\)]+");
        Matcher matcher = pattern.matcher(str);
        while(matcher.find()){
            strList.add(matcher.group());
        }
        return strList.get(0);
    }
}
