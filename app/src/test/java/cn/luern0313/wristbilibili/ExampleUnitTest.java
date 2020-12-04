package cn.luern0313.wristbilibili;


import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Date;

import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.TypeReference;
import cn.luern0313.lson.annotation.LsonDefinedAnnotation;
import cn.luern0313.lson.annotation.field.LsonAddPrefix;
import cn.luern0313.lson.annotation.field.LsonAddSuffix;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsNumber;
import cn.luern0313.lson.annotation.field.LsonBooleanFormatAsString;
import cn.luern0313.lson.annotation.field.LsonDateFormat;
import cn.luern0313.lson.annotation.field.LsonFieldCallMethod;
import cn.luern0313.lson.annotation.field.LsonNumberFormat;
import cn.luern0313.lson.annotation.field.LsonPath;
import cn.luern0313.lson.annotation.field.LsonReplaceAll;
import cn.luern0313.lson.annotation.method.LsonCallMethod;
import cn.luern0313.lson.element.LsonElement;
import cn.luern0313.lson.element.LsonObject;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    String json = "{\"dataMap\":{\"dataKey3\":{\"canRead\":true},\"dataKey4\":{\"canRead\":true},\"dataKey1\":{\"canRead\":true},\"dataKey2\":{\"canRead\":true}},\"store\":{\"book\":[{\"category\":\"reference\",\"author\":\"Nigel Rees\",\"title\":\"Sayings of the Century\",\"price\":true},{\"category\":\"fiction\",\"author\":\"Evelyn Waugh\",\"title\":\"Sword of Honour\",\"price\":12.99},{\"category\":\"fiction\",\"author\":\"Herman Melville\",\"title\":\"Mo by Dick\",\"isbn\":\"0-553-21311-3\",\"price\":8.99},{\"category\":\"fiction\",\"author\":\"J. R. R. Tolkien\",\"title\":\"The Lord of the Rings\",\"isbn\":\"0-395-19395-8\",\"price\":22.99}],\"bicycle\":{\"color\":\"red\",\"price\":12.99}}}";
    String json2 = "{\"code_value\":0.1,\"message\":\"success\",\"data\":{\"user_id\":9,\"timestamp\":1599665659,\"name\":\"luern0313\",\"lv\":6,\"coin\":5463.15,\"video\":[{\"video_id\":1,\"title\":\"1\",\"author\":{\"name\":\"luern0313\",\"uid\":9},\"img\":\"http://1.png\",\"state\":{\"like\":2.0,\"coin\":6}},{\"video_id\":2,\"title\":\"2\",\"author\":{\"name\":\"luern0313\",\"uid\":9},\"img\":\"http://2.png\",\"state\":{\"like\":23,\"coin\":66}},{\"video_id\":3,\"title\":\"3\",\"author\":{\"name\":\"luern0313\",\"uid\":9},\"img\":\"http://3.png\",\"state\":{\"like\":233,\"coin\":666}}],\"medal\":[[0,0,0,0],[0,0,1,4],[5,0,6,0]]}}";
    String json3 = "  { \"a\" : {  \"aa\": \"1\"},    \"video_author_uid\"   : {\"qqq\": {\"www\":123}}   }  ";

    @SneakyThrows
    @Test
    public void test()
    {
        LsonObject lsonObject = LsonUtil.parseAsObject(json2);
        //Object a = lsonObject.getFromPath("['data']['video'][?(@.state.like<=200)]['title']", Object.class);
        //System.out.println(a);

        BaseModel<UserModel<UserModel.UserVideoModel>> aaa = LsonUtil.fromJson(lsonObject, new TypeReference<BaseModel<UserModel<UserModel.UserVideoModel>>>(){});
        System.out.println();
        System.out.println(aaa);
        System.out.println();

        //Json3Model aaa = LsonUtil.fromJson(lsonObject, Json3Model.class);
        System.out.println(LsonUtil.toJson(aaa));

    }

    private static class Json3Model
    {
        @LsonPath("a.aa")
        String a;

        @LsonPath("video_author_uid.qqq.www")
        int videoAuthorUid;

        @LsonCallMethod
        void print()
        {
            System.out.println(videoAuthorUid);
        }
    }

    @ToString
    private static class BaseModel<T>
    {
        @LsonNumberFormat(digit = 1)
        @LsonPath
        String codeValue;

        @LsonBooleanFormatAsNumber(equal = {0.1, 123}, notEqual = {0.2, 0.1})
        @LsonPath("code_value")
        Boolean is_success;

        @LsonPath
        String message;

        @LsonBooleanFormatAsString(equal = {"success", "986456"}, notEqual = {"success", "fail"})
        @LsonPath("message")
        Boolean is_message;

        @LsonFieldCallMethod(deserialization = "aa")
        @LsonPath(value = "data", preClass = LsonElement.class)
        String text;

        @LsonPath("data")
        T data;

        private static Object aa(LsonElement map)
        {
            return map.toString();
        }
    }

    @ToString
    private static class UserModel<T>
    {
        @LsonPath
        int userId;

        @LsonPath
        StringBuilder name;

        @LsonAddPrefix("LV")
        @LsonPath
        String lv;

        @LsonDateFormat(value = "yyyy-MM-dd HH:mm", mode = LsonDateFormat.LsonDateFormatMode.SECOND)
        @LsonPath("timestamp")
        String date;

        @LsonPath("timestamp")
        Date date1;

        @LsonPath("timestamp")
        java.sql.Date date2;

        @LsonNumberFormat(digit = 0, mode = LsonNumberFormat.NumberFormatMode.DOWN)
        @LsonAddPrefix("硬妹币: ")
        @LsonReplaceAll(regex = "硬", replacement = "软")
        @LsonAddSuffix("个")
        @LsonPath()
        String coin;

        @LsonPath("video[0,2].title")
        ArrayList<String> videoTitle1;

        @LsonPath("video[1].title")
        ArrayList<String> videoTitle2;

        @LsonPath("video")
        ArrayList<T> videos;

        @LsonPath("medal")
        ArrayList<ArrayList<Integer>> medals;

        @LsonCallMethod(timing = {LsonCallMethod.CallMethodTiming.AFTER_DESERIALIZATION, LsonCallMethod.CallMethodTiming.AFTER_SERIALIZATION})
        void a()
        {
            videoTitle1.set(1, "9999");
            System.out.println(videoTitle1);
        }

        @ToString
        private static class UserVideoModel
        {
            @LsonPath()
            String videoId;

            @LsonPath()
            String title;

            @LsonNumberFormat(digit = 0, mode = LsonNumberFormat.NumberFormatMode.DOWN)
            @LsonAddSuffix("个")
            @LsonPath("state.like")
            String like;

            @LsonNumberFormat(digit = 0, mode = LsonNumberFormat.NumberFormatMode.DOWN)
            @LsonPath("state.coin")
            String coin;

            @LsonPath("img")
            String img;
        }
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @LsonDefinedAnnotation(config = ZheLiShiYiGeZiDingYiZhuJieConfig.class)
    public @interface ZheLiShiYiGeZiDingYiZhuJie
    {

    }

    public static class ZheLiShiYiGeZiDingYiZhuJieConfig implements LsonDefinedAnnotation.LsonDefinedAnnotationConfig
    {
        @Override
        public Object deserialization(Object value, Annotation annotation, Object object)
        {
            System.out.println("handle deserialization:" + value.toString());
            return value;
        }

        @Override
        public Object serialization(Object value, Annotation annotation, Object object)
        {
            System.out.println("handle serialization:" + value.toString());
            return value;
        }
    }
}
