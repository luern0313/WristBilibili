package cn.luern0313.wristbilibili;

import org.junit.Test;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;

import cn.luern0313.lson.LsonDefinedAnnotation;
import cn.luern0313.lson.LsonParser;
import cn.luern0313.lson.LsonUtil;
import cn.luern0313.lson.annotation.LsonPath;
import lombok.SneakyThrows;
import lombok.ToString;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @SneakyThrows
    @Test
    public void test()
    {
        String json = "{\"a\": [\"111\", \"222\"]}";
        LsonUtil.setLsonAnnotationListener(new E());
        A a = LsonUtil.fromJson(LsonParser.parseString(json), A.class);
        System.out.println();
        System.out.println(a.a.get(0));
        System.out.println(a.a.get(1));
    }

    class E implements LsonUtil.LsonAnnotationListener
    {
        @Override
        public Object handleAnnotation(Object value, Annotation annotation)
        {
            System.out.println((String) value);
            return "7777";
        }
    }

    @ToString
    public static class A
    {
        @Q("A")
        @LsonPath("a")
        private ArrayList<String> a;
    }

    @Target(ElementType.FIELD)
    @Retention(RetentionPolicy.RUNTIME)
    @LsonDefinedAnnotation
    public @interface Q
    {
        String value();
    }

}
