import lombok.Data;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Data
public class Student {

    Long id;
    Long age;

    static Logger log = Logger.getLogger(Student.class.getName());

    public Student(Long id, Long age) {
        this.id = id;
        this.age = age;
    }

    public static void main(String[] args) {
        List<Student> list = List.of(new Student(1L, 1L), new Student(2L, 2L));
        AtomicInteger j = new AtomicInteger();
        list.stream().map(o -> {
            o.setAge(222L);
            j.set(1);
            System.out.println(o);
            return null;
        }).collect(Collectors.toSet());
        System.out.println(list);
    }
}
