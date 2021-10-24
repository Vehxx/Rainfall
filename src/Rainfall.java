import java.lang.Math;
import java.util.concurrent.ThreadLocalRandom;

import org.joml.*;

public class Rainfall {

    private final float[][] dem;
    private final int sizeX;
    private final int sizeY;

    private float scale = 100.0f;
    private float density = 1.0f;
    private float friction = 0.1f;
    private float evaporationRate;
    private float depositionRate = 0.3f;

    public Rainfall(float[][] dem){
        this.dem = dem;
        this.sizeX = dem[0].length;
        this.sizeY = dem.length;

        evaporationRate = (float)1 / sizeX;
    }
    public void setScale(float scale){
        this.scale = scale;
    }
    public void setDensity(float density){
        this.density = density;
    }
    public void setFriction(float friction){
        this.friction = friction;
    }
    public void setEvaporationRate(float evaporationRate){
        this.evaporationRate = evaporationRate;
    }
    public void setDepositionRate(float depositionRate){
        this.depositionRate = depositionRate;
    }
    public float[][] getDem(){
        return dem;
    }

    private Vector3f getSurfaceNormal(int x, int y){

        float rootTwo = (float)Math.sqrt(2);
        
        // create the vector and add the 4 points directly adjacent to it
        Vector3f surfaceNormal = new Vector3f(0.15f).mul(new Vector3f(scale * (dem[x][y] - dem[x + 1][y]), 1.0f, 0.0f));
        surfaceNormal.add(new Vector3f(0.15f).mul(new Vector3f(scale * (dem[x - 1][y] - dem[x][y]), 1.0f, 0.0f)));
        surfaceNormal.add(new Vector3f(0.15f).mul(new Vector3f(0.0f, 1.0f, scale * (dem[x][y] - dem[x][y + 1]))));
        surfaceNormal.add(new Vector3f(0.15f).mul(new Vector3f(0.0f, 1.0f, scale * (dem[x][y - 1] - dem[x][y]))));

        // and the 4 diagonal adjacents
        surfaceNormal.add(new Vector3f(0.1f).mul(new Vector3f(scale * (dem[x][y] - dem[x + 1][y + 1]) / rootTwo, rootTwo, scale * (dem[x][y] - dem[x + 1][y + 1]) / rootTwo)));
        surfaceNormal.add(new Vector3f(0.1f).mul(new Vector3f(scale * (dem[x][y] - dem[x + 1][y - 1]) / rootTwo, rootTwo, scale * (dem[x][y] - dem[x + 1][y - 1]) / rootTwo)));
        surfaceNormal.add(new Vector3f(0.1f).mul(new Vector3f(scale * (dem[x][y] - dem[x - 1][y + 1]) / rootTwo, rootTwo, scale * (dem[x][y] - dem[x - 1][y + 1]) / rootTwo)));
        surfaceNormal.add(new Vector3f(0.1f).mul(new Vector3f(scale * (dem[x][y] - dem[x - 1][y - 1]) / rootTwo, rootTwo, scale * (dem[x][y] - dem[x - 1][y - 1]) / rootTwo)));

        // normalize the vector
        surfaceNormal.normalize();

        return surfaceNormal;
    }

    private int getRandomInt(int min, int max){
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private void raindrop(){
        // initialize the raindrop
        Vector2f location = new Vector2f(getRandomInt(1, sizeX - 2), getRandomInt(1, sizeY - 2));
        Vector2f speed = new Vector2f(0.0f);
        float volume = 1.0f;
        float percentSediment = 0.0f;

        // loop while the raindrop still exists
        while(volume > 0){
            Vector2f initialPosition = new Vector2f(location.x, location.y);
            Vector3f positionNormal = getSurfaceNormal((int)initialPosition.x, (int)initialPosition.y);

            // accelerate the raindrop using acceleration = force / mass
            speed.add(new Vector2f(positionNormal.x, positionNormal.z).div(volume * density));
            // update the position based on the new speed
            location.add(speed);
            // reduce the speed due to friction after the movement
            speed.mul(1.0f - friction);

            // check to see if the raindrop went out of bounds
            if(location.x >= sizeX - 1 || location.x < 1 || location.y >= sizeY - 1 || location.y < 1){
                break;
            }

            // compute the value of the maximum sediment and the difference between it and the percent sediment in the raindrop
            // positive numbers will cause erosion, negative numbers will cause deposition
            float maxSediment = volume * speed.length() * (dem[(int)initialPosition.x][(int)initialPosition.y] - dem[(int)location.x][(int)location.y]);
            if(maxSediment < 0.0f){
                maxSediment = 0.0f;
            }
            float sedimentDifference = maxSediment - percentSediment;

            // erode or deposit to the dem
            percentSediment += depositionRate * sedimentDifference;
            dem[(int)initialPosition.x][(int)initialPosition.y] -= volume * depositionRate * sedimentDifference;

            // evaporate the raindrop
            volume -= evaporationRate;
        }
    }

    public void simulate(int iterations){
        for(int i = 0; i < iterations; i++){
            raindrop();
        }
    }

}
