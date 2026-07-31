package cc.alking.example.springboot.beans.simplectorauto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BB {
    private AA aa;

    public AA getAa() {
        return aa;
    }

    @Autowired
    public BB(AA aa) {
        this.aa = aa;
    }
}
