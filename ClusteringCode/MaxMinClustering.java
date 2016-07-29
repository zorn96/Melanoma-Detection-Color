import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import javax.swing.JOptionPane;
import java.util.ArrayList;
public class MaxMinClustering
{
    Cluster[] clusters;
    Point[] points;
    File file;
    int dimensions, i, k, trueClusters;
    final double SSE_TOL = 1e-6;
    double old_SSE;
    double weight = -1;
    int width = -1;
    int height = -1;
    
    MaxMinClustering(String name, int i1, int k1, double w){
        i = i1;
        k = k1;
        file = new File(name);
        weight = w;
    }
    
    MaxMinClustering(String name, int i1, int k1){
        i = i1;
        k = k1;
        file = new File(name);
    }
    
    public void execute(){
        readData();
        assignMaxMinCenters();
        old_SSE = Double.MAX_VALUE;
        while(i > 0){
            cluster();
            double current_SSE = calcSSE();
            
            if((old_SSE - current_SSE)/current_SSE <= SSE_TOL){
                return;
            }
            
            old_SSE = current_SSE;
            if(i != 1)
                resetClusters();      
            i--;
        }
    }
    
    public void readData(){
        try{                        
            if(file.getName().contains(".ppm") || file.getName().contains(".PPM")){
                PPMReader myReader = new PPMReader(file);
                dimensions = 3;
                if(weight > 0){
                    myReader.readWeightedData(weight);
                    height = myReader.getHeight();
                    width = myReader.getWidth();
                    dimensions = 5;
                }else{
                    myReader.readUnweightedData();
                    height = myReader.getHeight();
                    width = myReader.getWidth();
                }
                points = myReader.getData();
                return;
            }
            
            Scanner reader = new Scanner(file);
            String line = reader.nextLine();            
            int numLines = Integer.parseInt(line.substring(0, line.indexOf(" ")).trim());
            points = new Point[numLines];
            if(line.lastIndexOf(" ") != line.indexOf(" ")){
                dimensions = Integer.parseInt(line.substring(line.indexOf(" ") + 1, line.lastIndexOf(" ")).trim());
                String tempTrue = line.substring(line.lastIndexOf(" ") + 1).trim();
                trueClusters = 0;
                if(!tempTrue.equals("")){
                    trueClusters = Integer.parseInt(tempTrue);
                    dimensions -= 1;
                }
                
            }else{
                dimensions = Integer.parseInt(line.substring(line.indexOf(" ") + 1).trim());
                trueClusters = 0;
            }
            
            int n = 0;
            while(n < numLines){
                String[] currLine = reader.nextLine().split(" ");
                double[] dims = new double[dimensions];
                for(int x = 0; x < dimensions; x++){
                    dims[x] = Double.parseDouble(currLine[x].trim());
                }
                Point p = new Point(dims);
                points[n] = p;
                n++;
            }
        }catch(IOException e){
            System.err.println("Error: File not found. Please check your file name and format.");
        }
    }
    
    public void assignMaxMinCenters(){
        int n = 1;
        ArrayList<Point> centers = new ArrayList(k);
        clusters = new Cluster[k];
        
        double[] dimens = new double[dimensions];
        for(int i = 0; i < dimens.length; i++){
            double total = 0.0;
            for(Point p : points){
                total += p.getDimension(i);
            }
            dimens[i] = total/points.length;
        }
        centers.add(new Point(dimens));
        clusters[0] = (new Cluster(centers.get(0)));
        
        while(n < k){
            double[] error = new double[points.length];
            
            for(int p = 0; p < points.length; p++){
                double minErr = Double.MAX_VALUE;
                for(Point c : centers){
                    double temp = Point.calcSE(points[p], c);
                    if(temp < minErr)
                        minErr = temp;
                }
                error[p] = minErr;
            }
            
            int maxErrLoc = 0;
            for(int q = 1; q < error.length; q++){
                if(error[q] > error[maxErrLoc]){
                    maxErrLoc = q;
                }
            }
            centers.add(points[maxErrLoc]);
            clusters[n] = new Cluster(points[maxErrLoc]);
            n++;
        }
    }
    
    public void cluster(){
        for(Point p : points){
            double[] error = new double[k];
            for(int i = 0; i < k; i++){
                error[i] = Point.calcSE(p, clusters[i].getCenter());
            }
            double minEr = error[0];
            int minLoc = 0;
            for(int x = 1; x < k; x++){
                if(error[x] < minEr){
                    minEr = error[x];
                    minLoc = x;
                }
            }
            clusters[minLoc].add(p);
        }
    }
    
    public double calcSSE(){
        double sse = 0.0;
        for(int q = 0; q < k; q++){
            clusters[q].recalculateCenter();
            sse += clusters[q].calcClusterSSE();
        }
        return sse;
    }
    
    public double calcSSW(){
        double ssw = 0.0;
        for(int q = 0; q < k; q++){
            ssw += clusters[q].calcClusterSSE();
        }
        
        return ssw;
    }
    
    public double calcEk(){
        double ek = 0.0;
        for(int q = 0; q < k; q++){
            ek += clusters[q].calcClusterSE(); 
        }
        
        return ek;
    }
    
    public double calcSSB(){
        double ssb = 0.0;
        double dataAverage[] = new double[dimensions];
        for(int p = 0; p < points.length; p++){
            dataAverage = Point.add(new Point(dataAverage), points[p]);
        }
        
        for(int d = 0; d < dimensions; d++){
            dataAverage[d] /= points.length;
        }

        
        for(int c = 0; c < k; c++){
            ssb += clusters[c].size() * Point.calcSE(new Point(dataAverage), clusters[c].getCenter());
        }
        
        return ssb;
    }
    
    public double calcEi(){
        //change to abs value
        double ei = 0.0;
        double dataAverage[] = new double[dimensions];
        for(int p = 0; p < points.length; p++){
            dataAverage = Point.add(new Point(dataAverage), points[p]);
        }
        
        for(int d = 0; d < dimensions; d++){
            dataAverage[d] /= points.length;
        }
        
        for(Point p: points){
            ei += Math.sqrt(Point.calcSE(new Point(dataAverage), p));
        }
        
        return ei;
    }
    
    public void resetClusters(){
        for(int q = 0; q < k; q++){
            clusters[q].resetCluster();
        }
    }
    
    public double calcLargestD(int nodeIndex){
        // We assume p = q = 2
        //calculates the largest possible D value with the given cluster as the node
        Cluster node = clusters[nodeIndex];
        double distance, scatter, scatter2;
        scatter = Math.sqrt(node.calcClusterSSE()/node.size()); 
        double[] dValues = new double[k];
        
        for(int x = 0; x < k; x++){
            if(x == nodeIndex)
                continue;
            scatter2 = Math.sqrt(clusters[x].calcClusterSSE()/clusters[x].size()); 
            distance = Math.sqrt(Point.calcSE(node.getCenter(), clusters[x].getCenter()));
            dValues[x] = (scatter + scatter2)/distance;
        }
        
        int maxDLoc = 0;
        for(int q = 1; q < k; q++){
            if(dValues[q] > dValues[maxDLoc])
                maxDLoc = q;
        }
        
        return dValues[maxDLoc];
    }
    
    public double findSmallestInterDistance(){
        //finds and returns the smallest possible distance between two cluster centers
        double smallest = Double.MAX_VALUE;
        for(int m = 0; m < k-1; m++){
            for(int n = m+1; n < k; n++){
                if(Point.calcSE(clusters[m].getCenter(), clusters[n].getCenter()) < smallest)
                    smallest = Point.calcSE(clusters[m].getCenter(), clusters[n].getCenter());
            }
        }
        return smallest;
    }
    
    public double findLargestInterDistance(){
        //finds and returns the largest possible distance between two cluster centers
        double largest = Double.MIN_VALUE;
        for(int m = 0; m < k-1; m++){
            for(int n = m+1; n < k; n++){
                if(Point.calcSE(clusters[m].getCenter(), clusters[n].getCenter()) > largest)
                    largest = Point.calcSE(clusters[m].getCenter(), clusters[n].getCenter());
            }
        }
        return Math.sqrt(largest); // regular Euclidean distance (not squared)
    }
    
    public Cluster[] getClusters(){
        return clusters;
    }
    
   public Point[] getPoints(){
       return points;
    }
    
    public int getNumPoints(){
        return points.length;
    }
    
    public int getTrueClusters(){
        return trueClusters;
    }
}
