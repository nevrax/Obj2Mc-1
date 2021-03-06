package com.garhoogin.obj2minecraft.world;

import com.garhoogin.obj2minecraft.ConverterGUI;
import com.garhoogin.obj2minecraft.MaterialSet;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;

/**
 * The {@code World} class contains a set of blocks, and the minimum and maximum
 * position of blocks.
 * 
 * @author Declan Moore
 */
public class World {
    
    private final List<Block> blocks;
    private int minX;
    private int minY;
    private int minZ;
    private int maxX;
    private int maxY;
    private int maxZ;
    
    
    /**
     * Create a new instance of {@code World}.
     */
    public World(){
        this.blocks = new ArrayList<>();
        this.maxX = 0;
        this.maxY = 0;
        this.maxZ = 0;
        this.minX = 0;
        this.minY = 0;
        this.minZ = 0;
    }
    
    static String getBlock(MaterialSet materialSet, String material, Color color){
        return materialSet.getBlock(color, material);
    }
    
    
    /**
     * Add a block to this world.
     * 
     * @param x           the X coordinate of the block
     * @param y           the Y coordinate of the block
     * @param z           the Z coordinate of the block
     * @param materialSet the material set to use to match blocks
     * @param material    the material to use to match blocks
     * @param color       the color to try to match
     */
    public void addBlock(int x, int y, int z, MaterialSet materialSet, String material, Color color){
        String block = getBlock(materialSet, material, color);
        synchronized(blocks){
            blocks.add(new Block(block, x, y, z));
            if(x < minX) minX = x;
            if(x > maxX) maxX = x;
            if(y < minY) minY = y;
            if(y > maxY) maxY = y;
            if(z < minZ) minZ = z;
            if(z > maxZ) maxZ = z;
        }
    }
    
    
    /**
     * Save this {@code World}.
     * 
     * @param progressWindow the progress window
     * @param outDirectory   the output directory
     * @throws IOException   if any files failed to be written
     */
    public void save(ConverterGUI.ProgressWindow progressWindow, String outDirectory) throws IOException{
        //subtract min from all coordinates
        int n = 0;
        ((JLabel) progressWindow.frame.getContentPane().getComponent(0)).setText("Blocks:");
        progressWindow.layersProgressBar.setValue(0);
        progressWindow.layersProgressBar.setMaximum(blocks.size());
        for(Block b : blocks){
            if(b == null){
                System.err.println("Null block " + n + "!");
                n++;
                continue;
            }
            b.x -= minX;
            b.y -= minY;
            b.z -= minZ;
            n++;
            //we don't need to update the progressbar for every block
            if((n & 0xFF) == 0) progressWindow.layersProgressBar.setValue(n);
        }
        progressWindow.layersProgressBar.setValue(n);
        //create regions.
        int sizeX = maxX - minX;
        int sizeY = maxY - minY;
        int sizeZ = maxZ - minZ;
        
        int chunksX = (sizeX + 15) / 16;
        int chunksZ = (sizeZ + 15) / 16;
        
        int regionsX = (chunksX + 31) / 32;
        int regionsZ = (chunksZ + 31) / 32;
        
        progressWindow.regionsProgressBar.setMaximum(regionsX * regionsZ);
        progressWindow.chunksProgressBar.setMaximum(32 * 32 * 2); //32x32, 2 stages
        
        System.out.println("Needed regions: " + regionsX + "x" + regionsZ);
        for(int x = 0; x < regionsX; x++){
            for(int z = 0; z < regionsZ; z++){
                System.out.println("Generating region (" + x + ", " + z + ")");
                Region.write(x, z, blocks, outDirectory, progressWindow);
                progressWindow.regionsProgressBar.setValue(
                    progressWindow.regionsProgressBar.getValue() + 1);
            }
        }
    }
    
}
