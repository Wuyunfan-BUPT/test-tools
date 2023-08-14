package org.apache.process.report_utils.testcase;

import org.apache.process.report_utils.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Created by wangtong.wt on 2017/3/20.
 *
 * @author wangtong.wt
 * @date 2017/03/20
 * @author wuyfee.
 * @date 2023/07/28
 */
@Data
public class TaskResult {

    /**
     * case run time.
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

        // Determine whether the historically running Case includes the current running Case
        // If it is included, judge whether the historical running Case is successful or failed. If it succeeds, ignore it. If it fails, it will overwrite it.
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

    /**
     *
     * @param repoName repository name.
     * @param repoBaseUrl test-code base url.
     * @param gitBranch repository beanch.
     * @param codePath test-code poth in repository.
     * @param githubToken GitHub access token.
     * @return markdown content.
     * @throws IOException exception.
     */
    public String toMarkdown(String repoName, String repoBaseUrl, String gitBranch, String codePath, String githubToken) throws IOException {
        MarkdownBuilder builder = MarkdownBuilder.builder();
        builder.addHeader("Test Cases Info", 2);
        MarkdownTableBuilder tableBuilder = MarkdownTableBuilder.builder();

        tableBuilder.addHead("Total", "Success✅", "Failure❌", "Error❌", "Skipped️↪️");
        tableBuilder.addRow(getTotalCount(), getSuccessCount(), getFailureCount(),
                getErrorCount(), getSkipCount());

        builder.addTable(tableBuilder);

        GetGithubRepoInfo getGithubRepoInfo = new GetGithubRepoInfo();
        HashMap<String, RepoFileInfo> fileInfoMap = new HashMap<>();

        String url = GetGithubRepoInfo.API_BASE_URL+ "/"+ repoName + "/contents/" + codePath;
        // get all files and their url in repository.
        getGithubRepoInfo.getAllFilePath(url, gitBranch, githubToken ,fileInfoMap);
        builder.addHeader("--------------------------------", 3);
        builder.addHeader(":x: Failed Case Detail", 3);

        List<CaseResult> allBadCase = new ArrayList<>(failureCaseMap.values());
        allBadCase.addAll(errorCaseMap.values());

        for (CaseResult badCase : allBadCase) {
            MarkdownLinkBuilder linkBuilder = MarkdownLinkBuilder.builder();

            String caseUrl = getGithubRepoInfo.getCaseUrl(fileInfoMap, githubToken, getClassName(badCase.getClassName()), badCase.getMethodName(), repoName, gitBranch);
            if(Objects.equals(caseUrl, "")) {
                caseUrl = repoBaseUrl;
            }
            linkBuilder.setLink(badCase.getClassName() + "." + badCase.getMethodName(), caseUrl);

            System.out.println(caseUrl);

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
        if(allBadCase.size()==0){
            builder.addHeader("All case Pass! ", 4);
        }

        writeContentToMarkdown(repoBaseUrl, builder, successCaseMap, fileInfoMap, ":white_check_mark: Success Cases", 3);
        writeContentToMarkdown(repoBaseUrl, builder, skipCaseMap, fileInfoMap, ":next_track_button: Skipped Cases", 3);
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

    /**
     * get class name.
     * @param name whole name, such as: com.alibaba.nacos.config.ConfigSyncTest.
     * @return class name. such as: ConfigSyncTest.
     */
    public String getClassName(String name){
        String[] classNameArray = name.contains(".") ? name.split("\\.") : name.split("/");
        return classNameArray[classNameArray.length-1];
    }

    /**
     * write case content to markdown
     *
     * @param repoBaseUrl repository base url.
     * @param builder Markdown builder.
     * @param caseMap case result map.
     * @param fileInfoMap repository file map.
     * @param title section title.
     * @param level font size level.
     */
    public void writeContentToMarkdown(String repoBaseUrl, MarkdownBuilder builder, Map<String, CaseResult> caseMap, HashMap<String, RepoFileInfo> fileInfoMap, String title, int level){
        builder.addHeader("--------------------------------", level);
        builder.addHeader(title, level);
        MarkdownBuilder contextBuilder = MarkdownBuilder.builder();
        //exceptionBuilder.newLine();
        contextBuilder.newLine();
        for (CaseResult caseResult : caseMap.values()) {
            MarkdownLinkBuilder linkBuilder = MarkdownLinkBuilder.builder();
            String caseUrl = repoBaseUrl;
            if(fileInfoMap.containsKey(getClassName(caseResult.getClassName()))){
                caseUrl = fileInfoMap.get(getClassName(caseResult.getClassName())).getFileUrl();
            }
            System.out.println(caseUrl);
            linkBuilder.setLink(caseResult.getClassName() + "." + caseResult.getMethodName(),
                    caseUrl);

            contextBuilder.addBoldText("Name: " + linkBuilder.build() + " Time: " + caseResult.getTime() + "s").newLine();
            contextBuilder.newLine();
        }
        builder.addCollapse("Case Detail", contextBuilder.build());
    }


}
