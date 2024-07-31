package com.zhuanzhu.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Liwq
 */
public class ListUtil {
    public static final int DEFAULT_PAGE_SIZE = 1000;

    public static <T> List<List<T>> getList(List<T> list, int pageSize) {
        // 输入参数校验
        if (list == null) {
            throw new IllegalArgumentException("List cannot be null.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("PageSize must be greater than 0.");
        }

        List<List<T>> listList = new ArrayList<>();
        int size = list.size();
        int pageCount = size / pageSize + 1;

        for (int i = 0; i < pageCount; i++) {
            int start = i * pageSize;
            int end = Math.min(size, (i + 1) * pageSize);
            // 使用Collections.unmodifiableList创建不可修改的列表视图，增加安全性
            listList.add(Collections.unmodifiableList(list.subList(start, end)));
        }
        return listList;
    }

    public static <T> List<List<T>> getListDefault(List<T> list) {

        return getList(list, DEFAULT_PAGE_SIZE);
    }

}
