
import java.util.ArrayList;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class V2_condition implements condition {


    private int X;
    private int Y;
    private int threshold;
    private int squareSize;



    public V2_condition(int x, int y, int threshold,int squareSize) {
        this.X = x;
        this.Y = y;
        this.threshold = threshold;
        this.squareSize = squareSize;
    }

    public boolean applyCondition(Image img) {
        return calcSquare(img,X,Y) > threshold;

    }

    private double calcSquare(Image img, int X, int Y) {
        int squareRadius = (squareSize-1)/2;
        double sum = 0.0;
        for (int i = X-squareRadius; i<= X+squareRadius; i++) {
            for (int j = Y-squareRadius; j<= Y+squareRadius; j++) {
                sum += img.getPixelAt(i,j);
            }
        }
        return sum / (squareSize * squareSize);
    }
}
