import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Erode {

    public static float[][] fillNoise(){
        float[][] noiseData = new float[512][512];

        FastNoiseLite noise = new FastNoiseLite();
        noise.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2);
        noise.SetFractalType(FastNoiseLite.FractalType.FBm);
        noise.SetFractalOctaves(12);
        noise.SetFrequency(0.002f);
        noise.SetFractalGain(0.5f);
        noise.SetFractalLacunarity(2.0f);
        noise.SetSeed(876509);

        for (int i = 0; i < 512; i++){
            for (int j = 0; j < 512; j++){
                noiseData[i][j] = noise.GetNoise(i, j);
            }
        }

        return noiseData;
    }

    public static void writeImage(float[][] img, String path){
        BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 512; i++){
            for (int j = 0; j < 512; j++){
                float floatValue = img[i][j];
                floatValue++;
                floatValue *= 127;
                int value = (int)floatValue;
                if(value > 255){
                    value = 255;
                }
                if(value < 0){
                    value = 0;
                }
                Color color = new Color(value, value, value);
                int rgb = color.getRGB();
                image.setRGB(i, j, rgb);
            }
        }

        File ImageFile = new File(path);
        try {
            ImageIO.write(image, "png", ImageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        float[][] dem = fillNoise();

        Rainfall rainfall = new Rainfall(dem);
        // simulating rainfall in a loop in order to save images and create a timelapse
        for(int i = 0; i < 200; i++) {
            rainfall.simulate(10000);
            dem = rainfall.getDem();
            writeImage(dem, "out/test_1/" + i + ".png");
        }
    }

}
