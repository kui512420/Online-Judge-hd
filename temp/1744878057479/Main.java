import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        // 创建 Scanner 对象以读取用户输入
        Scanner scanner = new Scanner(System.in);

        // 提示用户输入第一个整数
        int a = scanner.nextInt();

        // 提示用户输入第二个整数
        int b = scanner.nextInt();

        // 计算 a + b 的结果
        int sum = a + b;

        // 输出结果
        System.out.println(sum);

        // 关闭 Scanner 对象
        scanner.close();
    }
}