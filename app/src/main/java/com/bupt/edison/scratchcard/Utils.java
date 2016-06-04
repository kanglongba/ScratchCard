package com.bupt.edison.scratchcard;

import android.util.Log;

/**
 * Created by edison on 16/6/3.
 */
public class Utils{

    /**
     * 返回原始矩阵的抽样矩阵
     * @param lenght
     * @param width
     * @param penWidth
     * @return
     */
    public static int[] getPixels(int lenght,int width,int penWidth){
        int m = (int)Math.ceil(((double)width)/penWidth);
        int n = (int)Math.ceil(((double)lenght)/penWidth);
        int lastRow = lenght%penWidth==0?penWidth:lenght%penWidth;
        int lastLine = width%penWidth==0?penWidth:width%penWidth;
        int x,y; //像素在矩阵中的坐标
        int[] pixels = new int[m*n]; //抽样矩阵

        for(int i=0;i<n;i++){
            if(i==n-1){
                for (int j = 0; j < m; j++) {
                    if (j == m - 1) {
                        y = i * penWidth + lastRow / 2;
                        x = j * penWidth + lastLine / 2;
                    } else {
                        y = i * penWidth + lastRow / 2;
                        x = j * penWidth + penWidth / 2;
                    }
                    pixels[i*m+j]=width*y+x;
                }
            }else {
                for (int j = 0; j < m; j++) {
                    if (j == m - 1) {
                        y = i * penWidth + penWidth / 2;
                        x = j * penWidth + lastLine / 2;
                    } else {
                        y = i * penWidth + penWidth / 2;
                        x = j * penWidth + penWidth / 2;
                    }
                    pixels[i*m+j]=width*y+x;
                }
            }
        }

        return pixels;
    }

    /**
     * 检查擦除的比例
     * @param pixels
     * @param index
     * @return
     */
    public static float checkCell(int[] pixels,int[] index){
        int wipe=0;
        int total = index.length;
        for (int i:index){
            if(pixels[i]==0){ //当矩阵像素的颜色=0时,判断此像素点已经被擦除.
                wipe++;
            }
        }
        Log.d("edison wipe area",(float)wipe/total+"");
        return (float)wipe/total;
    }

}
