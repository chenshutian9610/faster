package org.triski.faster.commons.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

/**
 * @author (unknown - get from network)
 * @date 2018/12/18
 */
public class PictureCode {
    /**
     * outputStream 可以是 response::getOutputStream, 但 response 需要下列代码（设置不缓存）
     * <p>
     * response.setContentType("image/jpeg");
     * response.setHeader("Pragma", "no-cache");
     * response.setHeader("Cache-Control", "no-cache");
     * response.setDateHeader("Expires", 0);
     */
    public static String generate(OutputStream outputStream) throws IOException {
        final int width = 80, height = 30;//定义验证码图片的大小
        BufferedImage buffImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//在内存中创建图象
        Graphics2D g = buffImg.createGraphics();//为内存中要创建的图像生成画布，用于“作画”

        //画一个白色矩形，作为验证码背景
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);

        //画一个黑色矩形边框
        g.setColor(Color.BLACK);
        g.drawRect(0, 0, width - 1, height - 1);

        //画40条灰色的随机干扰线
        g.setColor(Color.GRAY);
        Random random = new Random();           //设置随机种子
        for (int i = 0; i < 40; i++) {          //设置40条干扰线
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(10);//返回0到10之间一个随机数
            int y2 = random.nextInt(10);
            g.drawLine(x1, y1, x1 + x2, y1 + y2);
        }

        //创建字体
        Font font = new Font("Times New Roman", Font.PLAIN, 18);
        g.setFont(font);

        int length = 4;                        //设置默认生成4个长度的验证码
        StringBuffer randomCode = new StringBuffer();
        for (int i = 0; i < length; i++) {    //取得4位数的随机字符串
            String strRand = String.valueOf(random.nextInt(10));//返回一个伪随机数，它是取自此随机数生成器序列的、在 0（包括）和指定值（不包括）之间均匀分布的 int 值
            int red = random.nextInt(255);
            int green = random.nextInt(255);
            int blue = random.nextInt(255);
            g.setColor(new Color(red, green, blue));    //获得一个随机红蓝绿的配合颜色
            g.drawString(strRand, 13 * i + 6, 16);//把该数字用画笔在画布画出，并指定数字的坐标
            randomCode.append(strRand);                 //把该数字加到缓存字符串中。用于等会生成验证码字符串set到session中用于校对
        }

        buffImg.flush();    //清除缓冲的图片
        g.dispose();        //释放资源

        //使用支持jpeg格式的 ImageWriter 将一个图像写入 OutputStream。而在客户端的img标签通过src来从中提取出jpeg图片
        ImageIO.write(buffImg, "jpeg", outputStream);
        outputStream.close();
        return new String(randomCode);
    }
}
