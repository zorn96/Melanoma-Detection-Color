import java.util.ArrayList;
public class Cluster
{
    ArrayList<Point> data;
    Point center;
    
    Cluster(Point initial){
        data = new ArrayList();
        center = initial;
    }
    
    public void add(Point p){
        data.add(p);
    }
    
    public void recalculateCenter(){
        double[] dimens = new double[center.getDimensions()];
        for(int i = 0; i < dimens.length; i++){
            double total = 0.0;
            for(Point p : data){
                total += p.getDimension(i);
            }
            dimens[i] = total/data.size();
        }
        center = new Point(dimens);
    }
    
    public void resetCluster(){
        data = new ArrayList();
        data.add(center);
    }
    
    public Point getCenter(){
        return center;
    }
    
    public int size(){
        return data.size();
    }
    
    public double calcClusterSSE(){
        double total = 0.0;
        for(Point p : data){
            total += Point.calcSE(p, center);
        }
        return total;
    }

    public double calcClusterSE(){
        double total = 0.0;
        for(Point p : data){
            total += Math.sqrt(Point.calcSE(p, center));
        }
        return total;
    }
    
    public boolean contains(Object o){
        Point p = (Point) o;
        return data.contains(p);
    }
    
    public ArrayList<Point> getPoints(){
        return data;
    }
}
