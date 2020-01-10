package cn.luern0313.wristbilibili;

import org.json.JSONException;
import org.json.JSONObject;
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

    }

    public class Student
    {
        String stuNo;
        public String stuName;
        public JSONObject score;
        public double total;
        public double avg;

        public Student(String stuNo, String stuName, double chinese, double math, double english)
        {
            try
            {
                this.stuNo = stuNo;
                this.stuName = stuName;
                this.score.put("chinese", chinese);
                this.score.put("math", math);
                this.score.put("english", english);
                this.total = chinese + math + english;
                this.avg = Math.round(this.total / 3 * 100) / 100.0;
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static class SortTools
    {
        public static void sort(Student[] students, String type)
        {
            for (int i = 0; i < students.length - 1; i++)
            {
                for (int j = 0; j < students.length - 1 - i; j++)
                {
                    if(students[j].score.optDouble(type) < students[j + 1].score.optDouble(type))
                    {
                        Student temp = students[j];
                        students[j] = students[j + 1];
                        students[j + 1] = temp;
                    }
                }
            }
        }
    }

    public static void display(Student[] students)
    {
        System.out.println("学号\t\t姓名\t语文\t数学\t英语\t总分\t平均分");
        for (Student student : students)
        {
            System.out.println(student.stuNo + "\t" + student.stuName + "\t" + student.score.optDouble("chinese") + "\t" + student.score.optDouble("math") + "\t" +
                                       student.score.optDouble("english") + "\t" + student.total + "\t" + student.avg);
        }
    }
}
