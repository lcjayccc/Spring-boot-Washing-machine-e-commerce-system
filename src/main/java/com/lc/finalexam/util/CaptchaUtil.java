package com.lc.finalexam.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtil {
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;
    private static final int CODE_LENGTH = 4;
    private static final char[] CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    
    /**
     * 生成随机验证码
     */
    public static String generateCaptchaCode() {
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARS[random.nextInt(CHARS.length)]);
        }
        return code.toString();
    }
    
    /**
     * 生成验证码图片
     */
    public static BufferedImage generateCaptchaImage(String code) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // 设置背景色
        g.setColor(new Color(240, 240, 240));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        
        // 绘制干扰线
        Random random = new Random();
        g.setColor(new Color(190, 190, 190));
        for (int i = 0; i < 20; i++) {
            int x1 = random.nextInt(WIDTH);
            int y1 = random.nextInt(HEIGHT);
            int x2 = x1 + random.nextInt(30) - 15;
            int y2 = y1 + random.nextInt(30) - 15;
            g.drawLine(x1, y1, x2, y2);
        }
        
        // 添加噪点
        for (int i = 0; i < 100; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            image.setRGB(x, y, random.nextInt(255));
        }
        
        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 28));
        
        // 绘制验证码
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(random.nextInt(100), random.nextInt(100), random.nextInt(100)));
            int degree = random.nextInt(30) - 15; // 旋转角度
            g.rotate(Math.toRadians(degree), 25 + i * 25, HEIGHT / 2);
            g.drawString(String.valueOf(code.charAt(i)), 20 + i * 25, 30);
            g.rotate(-Math.toRadians(degree), 25 + i * 25, HEIGHT / 2);
        }
        
        g.dispose();
        return image;
    }
    
    /**
     * 生成Base64编码的验证码图片
     */
    public static String generateBase64Captcha(String code) throws IOException {
        BufferedImage image = generateCaptchaImage(code);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
    }
} 