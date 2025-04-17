package org.example.dockernetwork.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.example.dockernetwork.Entity.Plan;
import org.example.dockernetwork.Mapper.PlanMapper;
import org.example.dockernetwork.Service.PlanService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PlanServiceImpl extends ServiceImpl<PlanMapper, Plan> implements PlanService {

    private PlanMapper planMapper;

    // 查询 type 为指定值的所有数据
    public List<Plan> getByType(String type) {
        LambdaQueryWrapper<Plan> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Plan::getType, type);
        return planMapper.selectList(queryWrapper);
    }

    // 查询 name 为指定值的数据
    public List<Plan> getByName(String name) {
        return planMapper.selectList(
                new LambdaQueryWrapper<Plan>().eq(Plan::getName, name)
        );
    }

    // 组合条件查询
    public List<Plan> getByContainerPair(String c1, String c2) {
        return planMapper.selectList(
                new LambdaQueryWrapper<Plan>()
                        .eq(Plan::getContainer1, c1)
                        .eq(Plan::getContainer2, c2)
        );
    }
}
