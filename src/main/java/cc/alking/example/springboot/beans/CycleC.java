package cc.alking.example.springboot.beans;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CycleC {

    private CycleA cycleA;

    public CycleA getCycleA() {
        return cycleA;
    }

    @Autowired
    public void setCycleA(CycleA cycleA) {
        this.cycleA = cycleA;
    }
}
