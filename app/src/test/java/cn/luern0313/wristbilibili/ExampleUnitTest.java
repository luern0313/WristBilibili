package cn.luern0313.wristbilibili;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    @Test
    public void test() throws InterruptedException
    {
        Person p = new Student();
        p.a();
    }

    public class Person
    {
        void a()
        {
            System.out.println("1");
        }
    }

    public class Student extends Person
    {
        void a()
        {
            System.out.println("1");
        }
    }


}
