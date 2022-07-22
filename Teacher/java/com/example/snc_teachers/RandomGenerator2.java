package com.example.snc_teachers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomGenerator2 {
    List<Integer> ints = new ArrayList<>();
    int i = 0;

    RandomGenerator2() {
        for (int i = 0; i < 99999; i++) {
            String.format("%05d",i);
            ints.add(i);
        }
        Collections.shuffle(ints);
    }

    int nextInt() {
        return ints.get(i++);
    }
}
