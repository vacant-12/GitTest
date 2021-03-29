package com.hogwartsmini.demo.dao;

import com.hogwartsmini.demo.common.MySqlExtensionMapper;
import com.hogwartsmini.demo.dto.task.QueryHogwartsTestTaskListDto;
import com.hogwartsmini.demo.dto.task.TaskDataDto;
import com.hogwartsmini.demo.entity.HogwartsTestTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HogwartsTestTaskMapper extends MySqlExtensionMapper<HogwartsTestTask> {

    /**
     * 统计总数
     * @param params
     * @return
     */
    Integer count(@Param("params") QueryHogwartsTestTaskListDto params);

    /**
     * 列表分页查询
     * @param params
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<HogwartsTestTask> list(@Param("params") QueryHogwartsTestTaskListDto params, @Param("pageNum") Integer pageNum,
                                @Param("pageSize") Integer pageSize);

    List<TaskDataDto> getTaskByType(@Param("createUserId") Integer createUserId);

    List<TaskDataDto> getTaskByStatus(@Param("createUserId") Integer createUserId);

    List<HogwartsTestTask> getCaseCountByTask(@Param("createUserId") Integer createUserId, @Param("start") Integer start,
                                              @Param("end") Integer end);

}