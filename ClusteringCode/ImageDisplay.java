import javax.swing.JFrame;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
public class ImageDisplay extends JFrame
{
    int width, height;
    MaxMinClustering m;
    JFrame myFrame;
    double weight;
    String name;
    Point[] points;
    Cluster[] clusters;
    
    ImageDisplay(String title, MaxMinClustering myM){
        super(title);
        name = title;
        m = myM;
        m.execute();
        width = myM.width;
        height = myM.height;
        setSize(width, height);
        weight = myM.weight;
        setResizable(false);
        setVisible(true);
        points = m.getPoints();
        clusters = m.getClusters();
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public void drawImage(){
        paint(this.getGraphics());
    }
    
    public void paint(Graphics g1){
        if(weight > 0){
            for(Cluster c : clusters){
                ArrayList<Point> points = c.getPoints();
                Point center = c.getCenter();
                int r = (int) (center.getDimension(2) * (255)/Math.sqrt(1-weight) + .5);
                int g = (int) (center.getDimension(3) * (255)/Math.sqrt(1-weight) + .5);
                int b = (int) (center.getDimension(4) * (255)/Math.sqrt(1-weight) + .5);
                Color clusterColor = new Color(r, g, b);
                g1.setColor(clusterColor);
                for(Point p : points){
                    int x = (int) (p.getDimension(0) * (width-1)/Math.sqrt(weight) + .5);
                    int y = (int) (p.getDimension(1) * (height-1)/Math.sqrt(weight) + .5);
                    g1.fillRect(x, y, 1, 1);
                }
            }
        }else{
            int x = 0;
            int y = 0;
            for(Point p : points){
                for(Cluster c : clusters){
                    if(c.contains(p)){
                        int r = (int) (c.getCenter().getDimension(0));
                        int g = (int) (c.getCenter().getDimension(1));
                        int b = (int) (c.getCenter().getDimension(2));
                        g1.setColor(new Color(r, g, b));
                        g1.fillRect(x, y, 1, 1);
                    }
                }
                x++;
                if(x >= width){
                    x = 0;
                    y++;
                }
            }
        }
    }
    
    public void storeImage(String rootFile){
        int k = m.getClusters().length;
        String w = "" + weight;
        rootFile = rootFile.substring(0, rootFile.indexOf("."));
        if(!w.equals("0"))
            w = w.substring(w.indexOf(".") + 1);
        File storedImage = new File(rootFile + "_" + name.substring(0, name.indexOf(" ")) + "_" + k + "_" + "Weight" + w + ".ppm");
        try{
            if(!storedImage.createNewFile()){
                storedImage.delete();
                storedImage.createNewFile();
            }
            
            FileOutputStream fwriter = new FileOutputStream(storedImage);
            BufferedOutputStream write = new BufferedOutputStream(fwriter);
            DataOutputStream fis = new DataOutputStream(write);
            fis.writeBytes("P6");
            fis.writeByte((int) (10 & 0xff)); //next line
            fis.writeBytes("" + width);
            fis.writeByte((int) (32 & 0xff)); //space
            fis.writeBytes("" + height);
            fis.writeByte((int) (10 & 0xff));
            fis.writeBytes("255");
            fis.writeByte((int) (10 & 0xff));
            
            int x = 0;
            if(weight > 0){
                for(Point p : points){
                    for(Cluster c : clusters){
                        if(c.contains(p)){
                            Point center = c.getCenter();
                            int r = (int) (center.getDimension(2) * (255)/Math.sqrt(1-weight) + .5);
                            int g = (int) (center.getDimension(3) * (255)/Math.sqrt(1-weight) + .5);
                            int b = (int) (center.getDimension(4) * (255)/Math.sqrt(1-weight) + .5);
                            fis.write(r);
                            fis.write(g);
                            fis.write(b);
                        }
                    }
                }
            }else{
                for(Point p : points){
                    for(Cluster c : clusters){
                        if(c.contains(p)){
                            int r = (int) (c.getCenter().getDimension(0));
                            int g = (int) (c.getCenter().getDimension(1));
                            int b = (int) (c.getCenter().getDimension(2));
                            fis.write(r);
                            fis.write(g);
                            fis.write(b);         
                        }
                    }
                }
            }
            fis.close();
        }catch(IOException e){
            System.err.println("Error creating and writing image to file.");
        }
    }
    
    public void repaint(){
        paint(this.getGraphics());
    }
}
