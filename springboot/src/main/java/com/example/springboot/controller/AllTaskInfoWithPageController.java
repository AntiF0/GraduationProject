// 简洁模式 返回所有的任务(既未删除也未完成)
package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.TaskInfo;
import com.example.springboot.mapper.TaskInfoMapper;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/alltaskinfopage")
public class AllTaskInfoWithPageController {
    // 通常是controller中引入service,service中引入mapper
    // 此处为了简化
    @Resource
    TaskInfoMapper taskInfoMapper;

    // 查找数据
    @GetMapping
    public Result<?> findPage(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(defaultValue = "") String search,
                              @RequestParam(defaultValue = "0") Integer userId) {
        LambdaQueryWrapper<TaskInfo> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(TaskInfo::getTUserId, userId);
        // 暂时只根据任务名和任务描述搜索
        if (StrUtil.isNotBlank(search)) {
            wrapper.like(TaskInfo::getTName, search).or().
                    like(TaskInfo::getTDescribe, search);
        }
        Page<TaskInfo> taskInfoPage = taskInfoMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return Result.success(taskInfoPage);
    }
}
