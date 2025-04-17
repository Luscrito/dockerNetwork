package org.example.dockernetwork.Controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.example.dockernetwork.Entity.Plan;
import org.example.dockernetwork.Service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/plan")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping("/get/{id}")
    public Plan getPlan(@PathVariable Integer id) {
        return planService.getById(id);
    }

    @GetMapping("/getByName/{name}")
    public List<Plan> getPlanByName(@PathVariable String name) {
        return planService.getByName(name);
    }

    @GetMapping("/list")
    public IPage<Plan> listPlan(@RequestParam(defaultValue = "1") int pageNum, @RequestParam(defaultValue = "30")int pageSize){
        Page<Plan> page = new Page<>(pageNum,pageSize);
        return planService.page(page);
    }

    @PostMapping
    public String addPlan(@RequestBody Plan plan) {
        Plan temp = planService.getById(plan.getId());
        if (temp == null) {
            planService.save(plan);
            return "添加成功";
        }
        else
            return "添加失败";
    }

    @DeleteMapping("/delete/{id}")
    public String deletePlan(@PathVariable Integer id) {
        Plan temp = planService.getById(id);
        if (temp != null) {
            planService.removeById(id);
            return "删除成功";
        }
        else
            return "删除失败";
    }
}
