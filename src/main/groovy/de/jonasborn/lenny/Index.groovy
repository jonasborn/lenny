package de.jonasborn.lenny

import java.util.function.Function

class Index {

    public static List<File> list(File dir) {
        def r = []
        dir.traverse(type: groovy.io.FileType.FILES) { it ->
            r.add(it)
        }
        return r
    }

    public static Long countRecrusive(File dir, Function<File, Boolean> condition) {
        long total = 0;
        dir.traverse(type: groovy.io.FileType.FILES) {
            if (condition.apply(it)) total++;
        }
        return total
    }


    public static File next(File dir, Function<File, Boolean> condition) {
        def r = null

        //Please find something better than throwing a exception
        try {
            dir.traverse(type: groovy.io.FileType.FILES) { it ->
                if (condition.apply(it)) {
                    r = it
                    throw new RuntimeException("Fucked")
                }
            }
        } catch (Exception e) {

        }

        return r
    }

}
