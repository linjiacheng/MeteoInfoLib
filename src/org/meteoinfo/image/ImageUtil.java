/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.meteoinfo.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.sanselan.ImageFormat;
import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import org.apache.sanselan.Sanselan;

/**
 *
 * @author Yaqiang Wang
 */
public class ImageUtil {
    /**
     * Read RGB array data from image file
     * @param fileName Image file name
     * @return RGB array data
     * @throws java.io.IOException
     * @throws org.apache.sanselan.ImageReadException
     */
    public static Array imageRead(String fileName) throws IOException, ImageReadException{
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        BufferedImage image;
        if (extension.equalsIgnoreCase("jpg") || extension.equalsIgnoreCase("jpeg")){
            image = ImageIO.read(new File(fileName));
        } else {
            image = Sanselan.getBufferedImage(new File(fileName));
        }
        return imageRead(image);
    }
    
    /**
     * Read RGB array data from image
     * @param image Image
     * @return RGB array data
     */
    public static Array imageRead(BufferedImage image){
        int xn = image.getWidth();
        int yn = image.getHeight();
        Array r = Array.factory(DataType.INT, new int[]{yn, xn, 3});
        Index index = r.getIndex();
        int rgb;
        Color color;
        for (int i = 0; i < yn; i++){
            for (int j = 0; j < xn; j++){
                rgb = image.getRGB(j, yn - i - 1);
                color = new Color(rgb);
                r.setInt(index.set(i, j, 0), color.getRed());
                r.setInt(index.set(i, j, 1), color.getGreen());
                r.setInt(index.set(i, j, 2), color.getBlue());
            }
        }
        return r;
    }
    
    /**
     * Create image from RGB(A) data array
     * @param data RGB(A) data array
     * @return Image
     */
    public static BufferedImage createImage(Array data) {
        int width, height;
        width = data.getShape()[1];
        height = data.getShape()[0];
        Color undefColor = Color.white;
        BufferedImage aImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Color color;
        Index index = data.getIndex();
        boolean isAlpha = data.getShape()[2] == 4;
        if (data.getDataType() == DataType.FLOAT || data.getDataType() == DataType.DOUBLE){
            float r, g, b;
            if (isAlpha) {
                float a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getFloat(index.set(i, j, 0));
                        g = data.getFloat(index.set(i, j, 1));
                        b = data.getFloat(index.set(i, j, 2));
                        a = data.getFloat(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getFloat(index.set(i, j, 0));
                        g = data.getFloat(index.set(i, j, 1));
                        b = data.getFloat(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        } else {
            int r, g, b;
            if (isAlpha) {
                int a;
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getInt(index.set(i, j, 0));
                        g = data.getInt(index.set(i, j, 1));
                        b = data.getInt(index.set(i, j, 2));
                        a = data.getInt(index.set(i, j, 3));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b) || Double.isNaN(a)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b, a);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            } else {
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        r = data.getInt(index.set(i, j, 0));
                        g = data.getInt(index.set(i, j, 1));
                        b = data.getInt(index.set(i, j, 2));
                        if (Double.isNaN(r) || Double.isNaN(g) || Double.isNaN(b)) {
                            color = undefColor;
                        } else {
                            color = new Color(r, g, b);
                        }
                        aImage.setRGB(j, height - i - 1, color.getRGB());
                    }
                }
            }
        }

        return aImage;
    }
    
    /**
     * Save image into a file
     * @param data RGB(A) data array
     * @param fileName Output image file name
     * @throws IOException 
     * @throws org.apache.sanselan.ImageWriteException 
     */
    public static void imageSave(Array data, String fileName) throws IOException, ImageWriteException{
        String extension = fileName.substring(fileName.lastIndexOf('.') + 1);
        BufferedImage image = createImage(data);  
        ImageFormat format = getImageFormat(extension);
        if (format == ImageFormat.IMAGE_FORMAT_JPEG){
            ImageIO.write(image, extension, new File(fileName));
        } else {
            Sanselan.writeImage(image, new File(fileName), format, null);       
        }
    }
    
    private static ImageFormat getImageFormat(String ext){
        ImageFormat format = ImageFormat.IMAGE_FORMAT_PNG;
        switch(ext.toLowerCase()){
            case "gif":
                format = ImageFormat.IMAGE_FORMAT_GIF;
                break;
            case "jpeg":
            case "jpg":
                format = ImageFormat.IMAGE_FORMAT_JPEG;
                break;
            case "bmp":
                format = ImageFormat.IMAGE_FORMAT_BMP;
                break;
            case "tif":
            case "tiff":
                format = ImageFormat.IMAGE_FORMAT_TIFF;
                break;
        }
        return format;
    }
}
