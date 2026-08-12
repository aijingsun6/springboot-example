package cc.alking.example.springboot.beans.cycle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CycleB {

    private CycleC cycleC;

    public CycleC getCycleC() {
        return cycleC;
    }

    @Autowired
    public void setCycleC(CycleC cycleC) {
        this.cycleC = cycleC;
    }
}
