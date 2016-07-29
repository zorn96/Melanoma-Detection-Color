import java.io.File;
import java.io.IOException;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
public class PPMDisplayer extends JFrame
{
    public static void main(String[] args){
        PPMDisplayer imageViewer = new PPMDisplayer(JOptionPane.showInputDialog("Input the image file you would like to view:"));
        imageViewer.displayImage();
    }
    
    File imageFile;
    Point[] myPoints;
    int width, height;
    
    PPMDisplayer(String name){
        super(name.substring(0, name.indexOf(".")));
        imageFile = new File(name);
        setVisible(true);
    }
    
    public void displayImage(){
        PPMReader reader = new PPMReader(imageFile);
        try{
            reader.readUnweightedData();
        }catch(IOException e){
            System.err.println("Unable to find file specified.");
            return;
        }
        
        width = reader.getWidth();
        height = reader.getHeight();
        myPoints = reader.getData();
        setSize(width, height);
        setResizable(false);
        paint(this.getGraphics());
        
    }
    
    public void paint(Graphics g1){
        int x, y;
        x = 0;
        y = 0;
        for(Point p : myPoints){
            int r = (int) p.getDimension(0);
            int g = (int) p.getDimension(1);
            int b = (int) p.getDimension(2);
            g1.setColor(new Color(r, g, b));
            g1.fillRect(x, y, 1, 1);
            x++;
            if(x >= width){
                x = 0;
                y++;
            }
        }
    }
    
    public void repaint(){
        paint(this.getGraphics());
    }
}
