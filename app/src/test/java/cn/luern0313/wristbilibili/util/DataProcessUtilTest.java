package cn.luern0313.wristbilibili.util;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * 被 luern0313 创建于 2020/4/25.
 */
public class DataProcessUtilTest
{
    public static void main(String[] args)
    {
        System.out.println(getPositionInList(new String[]{"1", "2"}, "2"));
        System.out.println(getPositionInList(new Integer[]{1, 2}, 2));
        System.out.println(getPositionInList(new String[]{"1", "2"}, "2"));
    }

    @Test
    private static <T> int getPositionInList(T[] list, T element)
    {
        for(int i = 0; i < list.length; i++)
            if(list[i].equals(element))
                return i;
        return -1;
    }
}