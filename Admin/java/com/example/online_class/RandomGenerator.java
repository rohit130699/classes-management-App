package com.example.online_class;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomGenerator {
    List<Integer> ints = new ArrayList<>();
    int i = 0;

    RandomGenerator() {
        for (int i = 0; i < 8999; i++) {
            String.format("%04d",i);
            ints.add(i);
        }
        Collections.shuffle(ints);
    }

    int nextInt() {
        return ints.get(i++);
    }
}