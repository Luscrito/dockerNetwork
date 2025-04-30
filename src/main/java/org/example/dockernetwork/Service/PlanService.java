package org.example.dockernetwork.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.dockernetwork.Entity.Plan;

import java.util.List;

public interface PlanService extends IService<Plan> {

    List<Plan> getByType(String type);

    List<Plan> getByName(String name);

    List<Plan> getByContainerPair(String c1, String c2);

}
