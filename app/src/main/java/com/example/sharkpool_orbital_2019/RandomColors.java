package com.example.sharkpool_orbital_2019;

import java.util.Arrays;
import java.util.Collections;
import java.util.Stack;

class RandomColors {
    private Stack<Integer> recycle, colors;

    public RandomColors() {
        colors = new Stack<>();
        recycle =new Stack<>();
        recycle.addAll(Arrays.asList(
                0xff8760CD,0xff00bcd4,0xffb7c05f,
                0xff6776ca,0xff0b7ad1,0xff03a9f4,
                0xff009688,0xff4caf50,0xff8bc34a,
                0xffAF8779,0xff9e9e9e,0xff8ba3af
                )
        );
    }

    public int getColor() {
        if (colors.size()==0) {
            while(!recycle.isEmpty())
                colors.push(recycle.pop());
            Collections.shuffle(colors);
        }
        Integer c= colors.pop();
        recycle.push(c);
        return c;
    }
}