package org.quartz;

import java.util.StringTokenizer;

public class tokenTest {
    public static void main(String[] args) {
        // creating string tokenizer
        StringTokenizer st = new StringTokenizer("Come to learn");

        // counting tokens
        System.out.println("Total tokens : " + st.countTokens());

        // checking tokens
        while (st.hasMoreTokens()) {
            System.out.println("Next token : " + st.nextToken());
        }
    }
}
