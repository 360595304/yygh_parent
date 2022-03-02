import java.util.HashMap;
import java.util.Map;

/**
 * @author suhu
 * @createDate 2022/3/2
 */
public class MapTest {

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<String, Object>() {{
            put("name", "zhangsan");
            put("age", 18);
            put("sex", "男");
        }};
        StringBuilder builder = new StringBuilder();
        map.forEach((key, value) -> {
            builder.append("**").append(key).append("**:").append(value).append(",");
        });
        builder.deleteCharAt(builder.length()-1);
        String s = builder.toString();
        System.out.println(s);
    }
}
