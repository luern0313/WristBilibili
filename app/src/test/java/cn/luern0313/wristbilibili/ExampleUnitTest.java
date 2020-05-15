package cn.luern0313.wristbilibili;

import org.junit.Test;

import java.util.ArrayList;

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
        ArrayList<Student> studentArrayList = new ArrayList<Student>()
        {{
            add(new Student("1.txt"));
        }};
        System.out.println(studentArrayList.get(0).stuNo);
        studentArrayList.get(0).stuNo = "2";
        System.out.println(studentArrayList.get(0).stuNo);
    }

    public class Student
    {
        String stuNo;

        Student(String stuNo)
        {
            this.stuNo = stuNo;
        }
    }


}
