package com.example.online_class;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomGenerator3 {
    List<Integer> ints = new ArrayList<>();
    int i = 0;

    RandomGenerator3() {
        for (int i = 0; i < 99; i++) {
            String.format("%02d",i);
            ints.add(i);
        }
        Collections.shuffle(ints);
    }

    int nextInt() {
        return ints.get(i++);
    }
}

