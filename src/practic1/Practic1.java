/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practic1;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;

/**
 *
 * @author Виктория
 */
public class Practic1 {
    public static final int cnst=8;
    public static double m = Math.sqrt(2)/Math.sqrt(cnst);
    public static double c = 1/Math.sqrt(2);
    public static double pi = 3.142857;
    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        File file = new File("im6.jpg");
        BufferedImage image = ImageIO.read(file);
        BufferedImage imageFiltered = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        
        int[][] Y = new int[image.getWidth()][image.getHeight()];//массив для хранения изображения в YUV(только Y)
        int[][] check;//массив для хранения изображения в YUV(только U)
        int[][] resultY = new int[image.getWidth()][image.getHeight()];//массив изображения после ДКП по Y
        
        int Y0;//канал Y YUV
        System.out.printf("Enter a max value of the coefficients> ");
        Scanner in = new Scanner(System.in);
        int coef=in.nextInt();
        //Проходимся по каждому пикселю исходного изображения, преобразуем RGB к YUV и записываем Y в массив
        for(int i = 0; i <image.getWidth(); i++){
            for(int j = 0; j <image.getHeight(); j++){
                Color col=new Color(image.getRGB(i,j));
                int [] yuv = new int[3];// vмассив для хранения YUV одного пикселя 
                int r=col.getRed() & 0xFF;
                int g=col.getGreen() & 0xFF;
                int b=col.getBlue() & 0xFF;
                
                rgb2yuv(r, g, b, yuv); // перевод из RGB к YUV
                Y0 = yuv[0];             
                Y[i][j] = Y0;
            }
        }
        
        
        
        resultY = process(Y,resultY.length, resultY[0].length, coef);        
        printRes(resultY, "outputY.txt");// сохраняем результат в файл
        
        check = check(image.getWidth(),image.getHeight(), coef);
        int y,u,v;
        int[] rgb = new int[3];
        for(int i = 0; i <check.length; i++){
            for(int j = 0; j <check[0].length; j++){
                y = check[i][j];
                u = 128; 
                v = 128;
                yuv2rgb(y, u, v,  rgb);
                imageFiltered.setRGB(i, j,new Color(rgb[0],rgb[1],rgb[2]).getRGB());
            }
        }
        ImageIO.write(imageFiltered, "jpg", new File("check.jpg"));
    }
    
    public static int[][] process(int[][] YUV,int w, int h, int coef){
        int[][] result = new int[w][h];
        int[][] mas;//массив для хранения блока 8х8
        int[][] masDct;//массив для хранения блока 8х8 после ДКП
        for(int i = 0; i <YUV.length; i+=cnst){
            for(int j = 0; j <YUV[0].length; j+=cnst){
                mas = clonePartArray(YUV, i,j);// копируем в mas блок 8х8
                masDct = dctTransform(mas, coef);// применяем ДКП к блоку 8х8
                result = cloneArray(masDct,result, i, j );// сохраняем результат в большой массив
            }
        }
        return result;
    }
      
    
    // Функция копироания части изображения в блок 8х8
    public static int[][] clonePartArray(int[][] src, int id, int jd ) {//int[][] src - откуда копируем, 
                                                                        //int id - с какой строки, 
                                                                        //int jd - с какого столбца 
        int[][] target = new int[8][8];
        for (int i = id, j=0; i < id + 8; i++,j++) {
            System.arraycopy(src[i], jd, target[j], 0, cnst);
        }
        return target;
    }
    // Функция копирования из блока 8х8 в исходный массив
    public static int[][] cloneArray(int[][] src, int[][] target, int id, int jd ) {//double[][] src - откуда копируем,
                                                                                             //double[][] target - куда копируем,
                                                                                             //int id - в какую строку, 
                                                                                             //int jd - с какой столбец
        for (int i = 0; i < cnst; i++,id++) {
            System.arraycopy(src[i], 0, target[id], jd, cnst);
        }
        return target;
    }
    // функция сохранения массива в файл
    public static void printRes(int[][] res, String name) {
        PrintWriter bw;
            try{
                bw = new PrintWriter(new FileWriter(name));
                for(int i = 0; i <res.length; i++){
                    for(int j = 0; j <res[0].length; j++){
                        bw.write(res[i][j]+" ");    
                    }
                    bw.write("\r\n"); 
                }
                
                bw.close();
        } catch (Exception ex) {
            
        }
    }
    //функция перевода из RGB в YUV одного пиксля
    public static void rgb2yuv(int r, int g, int b, int[] yuv) {
      int y, u, v;
      y = ((r *  39191 + g *  76939 + b *  14942) >> 17);
      u = 128;
      v = 128;
      
      yuv[0] =  clip(y);
      yuv[1] = u;
      yuv[2] = v;
    }
    public static double[][] cosKoef(int coef){
        double[][] cos = new double[cnst][cnst];
        for (int i = 0; i < coef; i++) { 
            for (int j = 0; j < coef; j++) { 
                cos[i][j] = Math.cos((2*i+1)*j*Math.PI/(2*cnst));
            }
        }
        for (int i = coef; i < cnst; i++) { 
            for (int j = coef; j < cnst; j++) {
                cos[i][j] = 0;
            }
        }
        return cos;
    }
    // Дискретное косинус-преобразование
    public static int[][] dctTransform(int matrix[][], int coef) { 
        int i, j, u, v; 
        int[][] dct = new int[cnst][cnst]; // массив для хранения коэффициентво ДКП
        double[][] cos = cosKoef(coef);
        double ci, cj, sum; 

        for (u = 0; u < cnst; u++) { 
            for (v = 0; v < cnst; v++) { 
                if (u == 0) 
                    ci = c; 
                else
                    ci = 1; 

                if (v == 0) 
                    cj = c; 
                else
                    cj =1; 

                sum = 0; 
                for (i = 0; i < cnst; i++) { 
                    for (j = 0; j < cnst; j++) {
                        sum += matrix[i][j]*cos[i][u]*cos[j][v];
                    } 
                } 
                sum*=((ci*cj)/4.0);
                dct[u][v] = (int) sum; 
            } 
        } 
        return dct;
    } 
    //Обрезает значение до [0..255]
    public static int clip(int value) {
      return ((value < 0) ? 0 : ((value > 255) ? 255 : value));
    }  
    
    public static int[][] loadArrayFromFile(String path, int w, int h) throws IOException {
        int[][] arr = new int[w][h];
        BufferedReader br = new BufferedReader(new FileReader(path));

        List<String> lines = new ArrayList<>();
        while (br.ready()) {
          lines.add(br.readLine());
        }       

        for (int i = 0; i < h; i++) {
          for (int j = 0; j < w; j++) {
            String[] line = lines.get(i).split(" ");
            arr[i][j] = Integer.parseInt(line[j]);
          }
        }   
        
        return arr;
    }
    
    public static int[][] applyIDCT(int matrix[][], int coef) {
        int[][] dct = new int[cnst][cnst];
        double[][] cos = cosKoef(coef);
        double ci, cj, sum;
        for (int i=0;i<cnst;i++) {
          for (int j=0;j<cnst;j++) {
            sum = 0;
            for (int u=0;u<cnst;u++) {
              for (int v=0;v<cnst;v++) {
                   if (u == 0) 
                    ci = c; 
                else
                    ci = 1; 

                if (v == 0) 
                    cj = c; 
                else
                    cj =1; 
                sum+=(ci*cj)/4.0*cos[i][u]*cos[j][v]*matrix[u][v];
              }
            }
            dct[i][j]=(int)sum;
          }
        }
        return dct;
    }
    
    public static int[][] check(int w, int h, int coef) throws IOException{
        int[][] check = loadArrayFromFile("outputY.txt",w,h);
        int[][] mas;//массив для хранения блока 8х8
        int[][] masIdct;//массив для хранения блока 8х8 после обратного ДКП
        int[][] result = new int[w][h];
        for(int i = 0; i <check.length; i+=cnst){
            for(int j = 0; j <check[0].length; j+=cnst){
                mas = clonePartArray(check, i,j);// копируем в mas блок 8х8
                masIdct = applyIDCT(mas, coef);// применяем ДКП к блоку 8х8
                result = cloneArray(masIdct,result, i, j );// сохраняем результат в большой массив
            }
        }
        return result;
    }
    
    //Конвертирует yuv2rgb     
    public static void yuv2rgb(int y, int u, int v, int[] rgb) {
      int r, g, b;

      r = (int)(y + (1441*(v - 128) >> 10));                    
      g = (int)(y - ((354*(u - 128) + 734*(v - 128)) >> 10));   
      b = (int)(y + (1822*(u - 128) >> 10));                    

      rgb[0] = clip(r);
      rgb[1] = clip(g);
      rgb[2] = clip(b);
    }
    
}
    

