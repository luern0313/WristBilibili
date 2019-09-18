package cn.luern0313.wristbilibili.api;

import java.util.ArrayList;

/**
 * 被 luern0313 创建于 2019/5/27.
 */

public class DownloadApi
{
    public DownloadApi()
    {

    }

    public static ArrayList<DownloadItem> fakeData() //调试ui用
    {
        ArrayList<DownloadItem> arrayList = new ArrayList<>();
        arrayList.add(new DownloadItem(2, "正在下载", "", 0));
        arrayList.add(new DownloadItem(1, "电影《大侦探皮卡丘》玩整版1080P", "http://i0.hdslb.com/bfs/archive/7917b40671d87ec6d66a8b5d9bceb5de40aa0ee5.jpg", 366592));
        arrayList.add(new DownloadItem(2, "下载完成", "", 0));
        arrayList.add(new DownloadItem(0, "手绘700帧！完美还原蔡徐坤打篮球！鸡你太美~", "http://i1.hdslb.com/bfs/archive/a6e5c6f71bf5f13fb8d16c2753a9337ff85a5f89.png", 2662));
        return arrayList;
    }

    public static class DownloadItem
    {
        public int mode;
        public String title;
        public String cover;
        public long size;
        DownloadItem(int m, String t, String c, long s)
        {
            mode = m;
            title = t;
            cover = c;
            size = s;
        }
    }
}
