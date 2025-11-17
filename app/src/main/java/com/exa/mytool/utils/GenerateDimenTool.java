package com.aam.mida.mida_yk.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by 江坚 on 2022/11/25
 * Description：
 */
public class GenerateDimenTool {

    public static void main(String[] args) {
        //480x320 800x400 854x480 888x540 1024x600 1024x768 1184x720 1196x720 1280x720 1812x1080 1920x1080 2560x1440

        {

            int with = 1920;
            double multiple = 1;
            StringBuilder builder = new StringBuilder();
            //添加xml开始的标签
            String xmlStart = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n <resources>\n";
            builder.append(xmlStart);
            for (int i = 0; i <= with; i++) {
                String dimenName = "    <dimen name=\"px" + i + "\">";
                String end = "px</dimen>";
                builder.append(dimenName).append(i / multiple).append(end).append("\n");
            }
            //添加sp
           /* builder.append("\n\n\n<!--sp-->\n");
            for (int i = 1; i <= 60; i++) {
                String dimenName = "    <dimen name=\"sp" + i + "\">";
                String end = "sp</dimen>";
                builder.append(dimenName).append(i).append(end).append("\n");
            }*/
            //添加xml的尾标签
            builder.append("</resources>");
            String dimensFile = "./app/src/main/res/values/dimens.xml";
            String dimensFile_1920 = "./app/src/main/res/values-1920x1080/dimens.xml";
            PrintWriter out = null;
            PrintWriter out_1920 = null;
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(dimensFile)));
                out_1920 = new PrintWriter(new BufferedWriter(new FileWriter(dimensFile_1920)));
                out.println(builder);
                out_1920.println(builder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.close();
            out_1920.close();
        }
        //1280x720
        {

            int with = 1920;
            double multiple = 1.5;
            StringBuilder builder = new StringBuilder();
            //添加xml开始的标签
            String xmlStart = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n <resources>\n";
            builder.append(xmlStart);
            for (int i = 0; i <= with; i++) {
                String dimenName = "    <dimen name=\"px" + i + "\">";
                String end = "px</dimen>";
                builder.append(dimenName).append(i / multiple).append(end).append("\n");
            }
            //添加sp
            /*builder.append("\n\n\n<!--sp-->\n");
            for (int i = 1; i <= 60; i++) {
                String dimenName = "    <dimen name=\"sp" + i + "\">";
                String end = "sp</dimen>";
                builder.append(dimenName).append(i).append(end).append("\n");
            }*/
            //添加xml的尾标签
            builder.append("</resources>");
            String dimensFile = "./app/src/main/res/values-1280x720/dimens.xml";
            PrintWriter out = null;
            try {
                out = new PrintWriter(new BufferedWriter(new FileWriter(dimensFile)));
                out.println(builder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            out.close();
        }


    }

}
