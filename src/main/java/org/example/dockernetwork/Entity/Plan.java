package org.example.dockernetwork.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Data
@TableName("test_plan")
public class Plan {
    private int id;
    private String name;
    private String type;
    private int packetSize;
    private String container1;
    private String container2;

    public Plan(int id, String container2, String container1, int packetSize, String type, String name) {
        this.id = id;
        this.container2 = container2;
        this.container1 = container1;
        this.packetSize = packetSize;
        this.type = type;
        this.name = name;
    }

}
