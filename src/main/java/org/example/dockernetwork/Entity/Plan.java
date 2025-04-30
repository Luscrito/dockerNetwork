package org.example.dockernetwork.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("plan")
public class Plan {
    private Integer id;
    private String name;
    private String type;
    private Integer size;
    private Integer time;
    private Integer num;
    private String velocity;
    private String container1;
    private String container2;
    private String container2Ip;
}
