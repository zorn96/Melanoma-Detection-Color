public class Point
{
    double[] coordinates;
    
    Point(double[] c){
        coordinates = new double[c.length];
        for(int d = 0; d < c.length; d++){
            coordinates[d] = c[d];
        }
    }
    
    public int getDimensions(){
        return coordinates.length;
    }
    
    public double[] getCoordinates(){
        return coordinates;
    }
    
    public double getDimension(int i){
        return coordinates[i];
    }
    
    public static double calcSE(Point p1, Point p2){
        double total = 0.0;
        double temp;
        for(int d = 0; d < p1.coordinates.length; d++){
            temp = p1.coordinates[d] - p2.getDimension(d);
            total += temp * temp;
        }
        return total;
    }
    
    public static double[] add(Point p1, Point p2){
        double newPoint[] = new double[p1.coordinates.length];
        for(int d = 0; d < p1.coordinates.length; d++){
            newPoint[d] = p1.getDimension(d) + p2.getDimension(d);
        }
        return (newPoint);
    }
}
