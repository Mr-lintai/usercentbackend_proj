package com.yupi.usercent.service;

/**
 * @author lintai
 * @version 1.0
 */
public class InsertSort {
    public static void main(String[] args) {
        int[] arr = { 7, 9, 3, 4, 1 };
    }

    public int search(int[] nums, int target) {
        if(target < nums[0] || target > nums[nums.length - 1]) {
            return -1;
        }
        int left = 0, right = nums.length;
        //左开右闭
        while (left < right) {
            int mid = left + (right - left) / 2;
            if(target < nums[mid]){
                right = mid;
            }else if(target > nums[mid]){
                left = mid + 1;
            }else{
                return mid;
            }
        }
        return -1;
    }
}
