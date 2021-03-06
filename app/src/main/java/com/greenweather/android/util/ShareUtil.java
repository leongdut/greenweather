package com.greenweather.android.util;

import android.content.Context;

import com.greenweather.android.R;
import com.mob.MobSDK;

import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * Created by Administrator on 2018/5/19.
 */

public class ShareUtil {
    public static void showShare(Context context){
        MobSDK.init(context,"25e31a094806d","adb1a3d63eb89024f3dadc3132f1ae11");
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

    // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间等使用
        oks.setTitle("林鹏泽的绿茵天气app已经上线");
    // titleUrl是标题的网络链接，QQ和QQ空间等使用
        oks.setTitleUrl("http://leongdut.github.io/greenweather");
    // text是分享文本，所有平台都需要这个字段
        oks.setText("绿茵天气温馨提示，今天天气状况良好");
    // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
    //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片
    // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://leongdut.github.io/greenweather");
    // comment是我对这条分享的评论，仅在人人网和QQ空间使用
        oks.setComment("林鹏泽的绿茵天气app已经上线");
    // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(context.getString(R.string.app_name));
    // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://leongdut.github.io");

    // 启动分享GUI
        oks.show(context);
    }
}
