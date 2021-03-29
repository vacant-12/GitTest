package com.hogwartsmini.demo.util;

import com.alibaba.fastjson.JSONObject;
import com.hogwartsmini.demo.common.ServiceException;
import com.hogwartsmini.demo.constants.Constants;
import com.hogwartsmini.demo.dto.RequestInfoDto;
import com.hogwartsmini.demo.entity.HogwartsTestTask;
import com.offbytwo.jenkins.JenkinsServer;
import com.offbytwo.jenkins.client.JenkinsHttpClient;
import com.offbytwo.jenkins.model.Job;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class JenkinsUtil {


    /**
     * 获取执行测试的Job名称
     * @return
     */

    public static String getStartTestJobName(Integer createUserId){
        String jobName = "hogwarts_test_mini_start_test_"+createUserId;
        return jobName;
    }

    /**
     * 获取执行测试的Job名称
     * @return
     */

    public static String getJobSignByName(String jobName){
        if(StringUtils.isEmpty(jobName) || !jobName.contains("_")){
            return "";
        }
        String jobSign = jobName.substring(0, jobName.lastIndexOf("_"));
        return jobSign;
    }


    public static void build(String jobName,String userId,String remark,String testCommand) throws IOException, URISyntaxException {
        ClassPathResource classPathResource = new ClassPathResource("JenkinsConfigDir/hogwarts_test_jenkins_show.xml");
        InputStream inputStream = classPathResource.getInputStream();

        String jobConfigXml = FileUtil.getText(inputStream);
        String baseUrl = "http://stuq.ceshiren.com:8080/";
        String userName = "hogwarts";
        String password = "hogwarts123";

        //String jobName = "test19";

        JenkinsHttpClient jenkinsHttpClient = new JenkinsHttpClient(new URI(baseUrl),userName,password);
        String jenkinsVersion = jenkinsHttpClient.getJenkinsVersion();
        System.out.println("jenkinsVersion== "+ jenkinsVersion);

        JenkinsServer jenkinsServer = new JenkinsServer(jenkinsHttpClient);

        jenkinsServer.createJob(jobName,jobConfigXml,true);

        Map<String, Job> jobMap = jenkinsServer.getJobs();

        Job job = jobMap.get(jobName);

        Map<String,String> map = new HashMap<>();
        map.put("userId",userId);
        map.put("remark",remark);
        map.put("testCommand",testCommand);

        //实际执行测试时使用的配置
        /*Map<String,String> map = new HashMap<>();
        map.put("aitestBaseUrl",userId);
        map.put("token",remark);
        map.put("testCommand",testCommand);
        map.put("updateStatusData","pwd");*/

        job.build(map,true);

        System.out.println("");

    }


    public static StringBuilder getUpdateTaskStatusUrl(RequestInfoDto requestInfoDto, HogwartsTestTask hogwartsTestTask) {

        StringBuilder updateStatusUrl = new StringBuilder();

        updateStatusUrl.append("curl -X PUT ");
        updateStatusUrl.append("\""+requestInfoDto.getBaseUrl() + "/task/status \" ");
        updateStatusUrl.append("-H \"Content-Type: application/json \" ");
        updateStatusUrl.append("-H \"token: "+requestInfoDto.getToken()+"\" ");
        updateStatusUrl.append("-d ");
        JSONObject json = new JSONObject();

        json.put("taskId",hogwartsTestTask.getId());
        json.put("status", Constants.STATUS_THREE);
        json.put("buildUrl","${BUILD_URL}");

        updateStatusUrl.append("'"+json.toJSONString()+"'");

        return updateStatusUrl;
    }
}
