package cc.alking.example.springboot.beans.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CycleA {

    private CycleB cycleB;

    public CycleB getCycleB() {
        return cycleB;
    }

    @Autowired
    public void setCycleB(CycleB cycleB) {
        this.cycleB = cycleB;
    }
}
