package com.sept3rd.bloggenerator;

import com.sept3rd.bloggenerator.ui.MainWindow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;

/**
 * Blog Generator Application Main Class
 * 博客生成器应用程序主类
 */
public class BlogGeneratorApp {
    private static final Logger logger = LoggerFactory.getLogger(BlogGeneratorApp.class);

    public static void main(String[] args) {
        logger.info("Starting Blog Generator Application...");
        
        // 设置系统外观
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            logger.warn("Failed to set system look and feel", e);
        }

        // 在事件调度线程中启动GUI
        SwingUtilities.invokeLater(() -> {
            try {
                new MainWindow().setVisible(true);
                logger.info("Blog Generator Application started successfully");
            } catch (Exception e) {
                logger.error("Failed to start application", e);
                JOptionPane.showMessageDialog(null, 
                    "应用程序启动失败: " + e.getMessage(), 
                    "错误", 
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}