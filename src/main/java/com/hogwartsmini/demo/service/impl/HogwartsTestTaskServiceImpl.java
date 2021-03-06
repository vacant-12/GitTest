package com.hogwartsmini.demo.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hogwartsmini.demo.common.ResultDto;
import com.hogwartsmini.demo.common.ServiceException;
import com.hogwartsmini.demo.common.TokenDto;
import com.hogwartsmini.demo.common.jenkins.JenkinsClient;
import com.hogwartsmini.demo.constants.Constants;
import com.hogwartsmini.demo.dao.HogwartsTestCaseMapper;
import com.hogwartsmini.demo.dao.HogwartsTestJenkinsMapper;
import com.hogwartsmini.demo.dao.HogwartsTestTaskCaseRelMapper;
import com.hogwartsmini.demo.dao.HogwartsTestTaskMapper;
import com.hogwartsmini.demo.dto.PageTableRequest;
import com.hogwartsmini.demo.dto.PageTableResponse;
import com.hogwartsmini.demo.dto.RequestInfoDto;
import com.hogwartsmini.demo.dto.jenkins.OperateJenkinsJobDto;
import com.hogwartsmini.demo.dto.task.AddHogwartsTestTaskDto;
import com.hogwartsmini.demo.dto.task.QueryHogwartsTestTaskListDto;
import com.hogwartsmini.demo.dto.task.TestTaskDto;
import com.hogwartsmini.demo.entity.HogwartsTestCase;
import com.hogwartsmini.demo.entity.HogwartsTestJenkins;
import com.hogwartsmini.demo.entity.HogwartsTestTask;
import com.hogwartsmini.demo.entity.HogwartsTestTaskCaseRel;
import com.hogwartsmini.demo.service.HogwartsTestTaskService;
import com.hogwartsmini.demo.util.JenkinsUtil;
import com.hogwartsmini.demo.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class HogwartsTestTaskServiceImpl implements HogwartsTestTaskService {

    @Autowired
    private HogwartsTestTaskMapper hogwartsTestTaskMapper;

    @Autowired
    private HogwartsTestJenkinsMapper hogwartsTestJenkinsMapper;

    @Autowired
    private HogwartsTestCaseMapper hogwartsTestCaseMapper;

    @Autowired
    private HogwartsTestTaskCaseRelMapper hogwartsTestTaskCaseRelMapper;

    @Autowired
    private JenkinsClient jenkinsClient;

    /**
     * ????????????????????????
     *
     * @param testTaskDto
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDto<HogwartsTestTask> save(TestTaskDto testTaskDto, Integer taskType) {

        StringBuilder testCommand = new StringBuilder();

        AddHogwartsTestTaskDto testTask = testTaskDto.getTestTask();
        List<Integer> caseIdList = testTaskDto.getCaseIdList();

        HogwartsTestJenkins queryHogwartsTestJenkins = new HogwartsTestJenkins();
        queryHogwartsTestJenkins.setId(testTask.getTestJenkinsId());
        queryHogwartsTestJenkins.setCreateUserId(testTask.getCreateUserId());

        HogwartsTestJenkins result = hogwartsTestJenkinsMapper.selectOne(queryHogwartsTestJenkins);

        if(Objects.isNull(result)){
            return ResultDto.fail("Jenkins????????????");
        }

        List<HogwartsTestCase> hogwartsTestCaseList = hogwartsTestCaseMapper.selectByIds(StrUtil.list2IdsStr(caseIdList));

        makeTestCommand(testCommand, result, hogwartsTestCaseList);

        HogwartsTestTask hogwartsTestTask = new HogwartsTestTask();

        hogwartsTestTask.setName(testTask.getName());
        hogwartsTestTask.setTestJenkinsId(testTask.getTestJenkinsId());
        hogwartsTestTask.setCreateUserId(testTask.getCreateUserId());
        hogwartsTestTask.setRemark(testTask.getRemark());

        hogwartsTestTask.setTaskType(taskType);
        hogwartsTestTask.setTestCommand(testCommand.toString());
        hogwartsTestTask.setCaseCount(caseIdList.size());
        hogwartsTestTask.setStatus(Constants.STATUS_ONE);
        hogwartsTestTask.setCreateTime(new Date());
        hogwartsTestTask.setUpdateTime(new Date());

        hogwartsTestTaskMapper.insert(hogwartsTestTask);

        if(Objects.nonNull(caseIdList) && caseIdList.size()>0){

            List<HogwartsTestTaskCaseRel> testTaskCaseList = new ArrayList<>();

            for (Integer testCaseId:caseIdList) {

                HogwartsTestTaskCaseRel hogwartsTestTaskCaseRel = new HogwartsTestTaskCaseRel();
                hogwartsTestTaskCaseRel.setTaskId(hogwartsTestTask.getId());
                hogwartsTestTaskCaseRel.setCaseId(testCaseId);
                hogwartsTestTaskCaseRel.setCreateUserId(hogwartsTestTask.getCreateUserId());
                hogwartsTestTaskCaseRel.setCreateTime(new Date());
                hogwartsTestTaskCaseRel.setUpdateTime(new Date());
                testTaskCaseList.add(hogwartsTestTaskCaseRel);
            }

            log.info("=====????????????????????????-????????????====???"+ JSONObject.toJSONString(testTaskCaseList));
            hogwartsTestTaskCaseRelMapper.insertList(testTaskCaseList);
        }

        return ResultDto.success("??????", hogwartsTestTask);
    }

    /**
     * ????????????????????????
     *
     * @param taskId
     * @param createUserId
     * @return
     */
    @Override
    public ResultDto<HogwartsTestTask> delete(Integer taskId, Integer createUserId) {
        HogwartsTestTask queryHogwartsTestTask = new HogwartsTestTask();

        queryHogwartsTestTask.setId(taskId);
        queryHogwartsTestTask.setCreateUserId(createUserId);

        HogwartsTestTask result = hogwartsTestTaskMapper.selectOne(queryHogwartsTestTask);

        //??????????????????????????????????????????????????????
        if (Objects.isNull(result)) {
            return ResultDto.fail("???????????????????????????");
        }
        hogwartsTestTaskMapper.deleteByPrimaryKey(taskId);

        return ResultDto.success("??????");
    }

    /**
     * ????????????????????????
     *
     * @param hogwartsTestTask
     * @return
     */
    @Override
    public ResultDto<HogwartsTestTask> update(HogwartsTestTask hogwartsTestTask) {
        HogwartsTestTask queryHogwartsTestTask = new HogwartsTestTask();

        queryHogwartsTestTask.setId(hogwartsTestTask.getId());
        queryHogwartsTestTask.setCreateUserId(hogwartsTestTask.getCreateUserId());

        HogwartsTestTask result = hogwartsTestTaskMapper.selectOne(queryHogwartsTestTask);

        //??????????????????????????????????????????????????????
        if (Objects.isNull(result)) {
            return ResultDto.fail("???????????????????????????");
        }

        result.setUpdateTime(new Date());
        result.setName(hogwartsTestTask.getName());
        result.setRemark(hogwartsTestTask.getRemark());

        hogwartsTestTaskMapper.updateByPrimaryKeySelective(result);

        return ResultDto.success("??????");
    }

    /**
     * ??????id??????
     *
     * @param taskId
     * @param createUserId
     * @return
     */
    @Override
    public ResultDto<HogwartsTestTask> getById(Integer taskId, Integer createUserId) {
        HogwartsTestTask queryHogwartsTestTask = new HogwartsTestTask();

        queryHogwartsTestTask.setId(taskId);
        queryHogwartsTestTask.setCreateUserId(createUserId);

        HogwartsTestTask result = hogwartsTestTaskMapper.selectOne(queryHogwartsTestTask);

        //??????????????????????????????????????????????????????
        if (Objects.isNull(result)) {
            ResultDto.fail("???????????????????????????");
        }

        return ResultDto.success("??????", result);
    }

    /**
     * ??????????????????????????????
     *
     * @param pageTableRequest
     * @return
     */
    @Override
    public ResultDto<PageTableResponse<HogwartsTestTask>> list(PageTableRequest<QueryHogwartsTestTaskListDto> pageTableRequest) {
        QueryHogwartsTestTaskListDto params = pageTableRequest.getParams();
        Integer pageNum = pageTableRequest.getPageNum();
        Integer pageSize = pageTableRequest.getPageSize();

        //??????
        Integer recordsTotal = hogwartsTestTaskMapper.count(params);

        //??????????????????
        List<HogwartsTestTask> hogwartsTestJenkinsList = hogwartsTestTaskMapper.list(params, (pageNum - 1) * pageSize, pageSize);

        PageTableResponse<HogwartsTestTask> hogwartsTestJenkinsPageTableResponse = new PageTableResponse<>();
        hogwartsTestJenkinsPageTableResponse.setRecordsTotal(recordsTotal);
        hogwartsTestJenkinsPageTableResponse.setData(hogwartsTestJenkinsList);

        return ResultDto.success("??????", hogwartsTestJenkinsPageTableResponse);
    }

    /**
     * ??????????????????????????????
     *
     * @param hogwartsTestTask
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ResultDto startTask(TokenDto tokenDto, RequestInfoDto requestInfoDto, HogwartsTestTask hogwartsTestTask) throws IOException {
        log.info("=====????????????-Service????????????====???"+ JSONObject.toJSONString(requestInfoDto)+"+++++"+JSONObject.toJSONString(hogwartsTestTask));
        if(Objects.isNull(hogwartsTestTask)){
            return ResultDto.fail("??????????????????????????????");
        }

        Integer defaultJenkinsId = tokenDto.getDefaultJenkinsId();

        if(Objects.isNull(defaultJenkinsId)){
            return ResultDto.fail("???????????????Jenkins");
        }

        HogwartsTestJenkins queryHogwartsTestJenkins = new HogwartsTestJenkins();
        queryHogwartsTestJenkins.setId(defaultJenkinsId);
        queryHogwartsTestJenkins.setCreateUserId(tokenDto.getUserId());

        HogwartsTestJenkins resultHogwartsTestJenkins = hogwartsTestJenkinsMapper.selectOne(queryHogwartsTestJenkins);

        if(Objects.isNull(resultHogwartsTestJenkins)){
            return ResultDto.fail("??????Jenkins?????????????????????");
        }

        HogwartsTestTask queryHogwartsTestTask = new HogwartsTestTask();

        queryHogwartsTestTask.setId(hogwartsTestTask.getId());
        queryHogwartsTestTask.setCreateUserId(hogwartsTestTask.getCreateUserId());

        HogwartsTestTask resultHogwartsTestTask = hogwartsTestTaskMapper.selectOne(queryHogwartsTestTask);


        if(Objects.isNull(resultHogwartsTestTask)){
            String tips = "?????????????????????";
            log.info(tips+queryHogwartsTestTask.getId());
            return ResultDto.fail(tips);
        }

        String testCommandStr =  hogwartsTestTask.getTestCommand();
        if(StringUtils.isEmpty(testCommandStr)){
            testCommandStr = resultHogwartsTestTask.getTestCommand();
        }

        if(StringUtils.isEmpty(testCommandStr)){
            return ResultDto.fail("?????????????????????????????????");
        }

        //??????????????????
        resultHogwartsTestTask.setStatus(Constants.STATUS_TWO);
        hogwartsTestTaskMapper.updateByPrimaryKeySelective(resultHogwartsTestTask);

        StringBuilder testCommand = new StringBuilder();

        //???????????????????????????????????????mvn test ??????
        testCommand.append(testCommandStr);
        testCommand.append(" \n");


        StringBuilder updateStatusUrl = JenkinsUtil.getUpdateTaskStatusUrl(requestInfoDto, resultHogwartsTestTask);

        //??????????????????
        Map<String, String> params = new HashMap<>();

        params.put("aitestBaseUrl",requestInfoDto.getBaseUrl());
        params.put("token",requestInfoDto.getToken());
        params.put("testCommand",testCommand.toString());
        params.put("updateStatusData",updateStatusUrl.toString());

        log.info("=====????????????Job?????????????????????====???" +JSONObject.toJSONString(params));
        log.info("=====????????????Job????????????????????????????????????====???" +updateStatusUrl);


        OperateJenkinsJobDto operateJenkinsJobDto = new OperateJenkinsJobDto();

        operateJenkinsJobDto.setParams(params);
        operateJenkinsJobDto.setTokenDto(tokenDto);
        operateJenkinsJobDto.setHogwartsTestJenkins(resultHogwartsTestJenkins);

        operateJenkinsJobDto.setParams(params);

        ResultDto resultDto = jenkinsClient.operateJenkinsJob(operateJenkinsJobDto);
        //???????????????????????????????????????
        if(0 == resultDto.getResultCode()){
            throw new ServiceException("?????????????????????-"+resultDto.getMessage());
        }
        return resultDto;
    }

    /**
     * ??????????????????????????????
     *
     * @param hogwartsTestTask
     * @return
     */
    @Override
    public ResultDto<HogwartsTestTask> updateStatus(HogwartsTestTask hogwartsTestTask) {
        HogwartsTestTask queryHogwartsTestTask = new HogwartsTestTask();

        queryHogwartsTestTask.setId(hogwartsTestTask.getId());
        queryHogwartsTestTask.setCreateUserId(hogwartsTestTask.getCreateUserId());

        HogwartsTestTask result = hogwartsTestTaskMapper.selectOne(queryHogwartsTestTask);

        //????????????????????????
        if (Objects.isNull(result)) {
            return ResultDto.fail("???????????????????????????");
        }

        //?????????????????????????????????????????????
        if(Constants.STATUS_THREE.equals(result.getStatus())){
            return ResultDto.fail("????????????????????????????????????");
        }
        result.setUpdateTime(new Date());

        //??????????????????????????????
        if(Constants.STATUS_THREE.equals(hogwartsTestTask.getStatus())){
            result.setBuildUrl(hogwartsTestTask.getBuildUrl());
            result.setStatus(Constants.STATUS_THREE);
            hogwartsTestTaskMapper.updateByPrimaryKey(result);
        }

        return ResultDto.success("??????");
    }

    /**
     *
     * @param testCommand
     * @param testCaseList
     */
    private void makeTestCommand(StringBuilder testCommand, HogwartsTestJenkins hogwartsTestJenkins, List<HogwartsTestCase> testCaseList) {

        //??????????????????
        testCommand.append("pwd");
        testCommand.append("\n");

        if(Objects.isNull(hogwartsTestJenkins)){
            throw new ServiceException("????????????????????????Jenkins????????????");
        }
        if(Objects.isNull(testCaseList) || testCaseList.size()==0){
            throw new ServiceException("??????????????????????????????????????????????????????");
        }

        String runCommand = hogwartsTestJenkins.getTestCommand();

        Integer commandRunCaseType = hogwartsTestJenkins.getCommandRunCaseType();
        String systemTestCommand = hogwartsTestJenkins.getTestCommand();

        if(StringUtils.isEmpty(systemTestCommand)){
            throw new ServiceException("?????????????????????????????????????????????????????????");
        }

        //??????????????????
        if(Objects.isNull(commandRunCaseType)){
            commandRunCaseType = 1;
        }

        //????????????
        if(commandRunCaseType==1){
            for (HogwartsTestCase hogwartsTestCase :testCaseList) {
                //??????????????????
                testCommand.append(systemTestCommand).append(" ");
                //??????????????????
                testCommand.append(hogwartsTestCase.getCaseData())
                        .append("\n");
            }
        }
        //????????????
        if(commandRunCaseType==2){

            String commandRunCaseSuffix = hogwartsTestJenkins.getCommandRunCaseSuffix();

            if(StringUtils.isEmpty(commandRunCaseSuffix)){
                throw new ServiceException("?????????????????????case????????????????????????????????????????????????");
            }

            for (HogwartsTestCase hogwartsTestCase :testCaseList) {

                //?????????????????????curl??????
                makeCurlCommand(testCommand, hogwartsTestCase, commandRunCaseSuffix);
                testCommand.append("\n");
                //??????????????????
                testCommand.append(systemTestCommand).append(" ");
                //????????????????????????
                testCommand.append(hogwartsTestCase.getCaseName())
                        //??????.?????????
                        .append(".")
                        //??????case????????????
                        .append(commandRunCaseSuffix)
                        .append(" || true")
                        .append("\n");
            }
        }



        log.info("testCommand.toString()== "+testCommand.toString() + "  runCommand== " + runCommand);


        testCommand.append("\n");
    }

    /**
     *  ?????????????????????curl??????
     * @param testCommand
     * @param hogwartsTestCase
     * @param commandRunCaseSuffix
     */
    private void makeCurlCommand(StringBuilder testCommand, HogwartsTestCase hogwartsTestCase, String commandRunCaseSuffix) {

        //??????curl??????????????????????????????????????????
        testCommand.append("curl ")
                .append("-o ");

        String caseName = hogwartsTestCase.getCaseName();

        if(StringUtils.isEmpty(caseName)){
            caseName = "???????????????????????????";
        }

        testCommand.append(caseName)
                .append(".")
                .append(commandRunCaseSuffix)
                .append(" ${aitestBaseUrl}/testCase/data/")
                .append(hogwartsTestCase.getId())
                .append(" -H \"token: ${token}\" ");

        //?????????????????????????????????????????????????????????
        testCommand.append(" || true");

        testCommand.append("\n");
    }

}
