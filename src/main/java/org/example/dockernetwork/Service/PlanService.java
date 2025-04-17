package org.example.dockernetwork.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dockernetwork.Entity.Plan;

import java.util.List;

public interface PlanService extends IService<Plan> {

    public List<Plan> getByType(String type);

    public List<Plan> getByName(String name);

    public List<Plan> getByContainerPair(String c1, String c2);

}
