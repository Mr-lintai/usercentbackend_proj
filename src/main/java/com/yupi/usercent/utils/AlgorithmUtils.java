package com.yupi.usercent.utils;

import java.util.List;

public class AlgorithmUtils {
    /**
     * 计算两组标签最小距离
     * @param tagList1
     * @param tagList2
     * @return
     */
    public static int editDistance(List<String> tagList1, List<String> tagList2) {

    int m = tagList1.size();
    int n = tagList2.size();
    // 初始化距离矩阵
    int[][] dp = new int[m + 1][n + 1];
    for (int i = 0; i <= m; i++) {
        dp[i][0] = i;
    }

    for (int j = 0; j <= n; j++) {
        dp[0][j] = j;
    }

    // 计算距离矩阵
    for (int i = 1; i <= m; i++) {
        for (int j = 1; j <= n; j++) {
            int cost = (tagList1.get(i - 1).equals(tagList2.get(j - 1))) ? 0 : 1;
            dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
        }
    }
        return dp[m][n];
    }

}