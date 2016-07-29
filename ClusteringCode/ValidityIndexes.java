import javax.swing.JOptionPane;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
public class ValidityIndexes
{
    int k_MAX, k_MIN, iter;
    String name;
    File bpiFile;
    double weight;
    MaxMinClustering[] clusterers;
    
    
    public static void main(String[] args){
        ValidityIndexes myValidity = new ValidityIndexes();
    }
    
    public ValidityIndexes(){
        String fileName = JOptionPane.showInputDialog("Enter the name of the BPI file or nothing if there is none:");
        if(!fileName.equals("")){
            bpiFile = new File(fileName);
            execute();
        }else{
            name = JOptionPane.showInputDialog("Enter the data file name:");
            weight = Double.parseDouble(JOptionPane.showInputDialog("Enter the weighting of spatial contiguity for this data set:"));
            if(weight < 0 || weight > 1){
                System.err.println("Error: Invalid weight given.");
                return;
            }
            k_MIN = Integer.parseInt(JOptionPane.showInputDialog("Enter the minimum number of clusters for this data:"));
            k_MAX = Integer.parseInt(JOptionPane.showInputDialog("Enter the maximum number of clusters for this data:"));        
            iter = Integer.parseInt(JOptionPane.showInputDialog("Enter the maximum number of iterations:"));
            System.out.println("Optimal Number of Clusters By Index:");
            clusterers = new MaxMinClustering[k_MAX - k_MIN + 1];
            for(int k = k_MIN; k <= k_MAX; k++){
                clusterers[k-k_MIN] = new MaxMinClustering(name, iter, k, weight);
                clusterers[k-k_MIN].execute();
            }
            int ch = CHIndex();
            int wb = WBIndex();
            int db = DBIndex();
            int rt = RTIndex();
            int pbm = PBMIndex();
            System.out.println("CH Index: " + ch + " clusters");
            System.out.println("WB Index: " + wb + " clusters");
            System.out.println("DB Index: " + db + " clusters");
            System.out.println("RT Index: " + rt + " clusters");
            System.out.println("PBM Index: " + pbm + " clusters");
            
            
            ImageDisplay i1 = new ImageDisplay("CH Index", new MaxMinClustering(name, iter, ch, weight));
            i1.drawImage();
            //i1.storeImage(name);
            
            ImageDisplay i2 = new ImageDisplay("WB Index", new MaxMinClustering(name, iter, wb, weight));
            i2.drawImage();
            //i2.storeImage(name);

            ImageDisplay i3 = new ImageDisplay("DB Index", new MaxMinClustering(name, iter, db, weight));
            i3.drawImage();
            //i3.storeImage(name);
            
            ImageDisplay i4 = new ImageDisplay("RT Index", new MaxMinClustering(name, iter, rt, weight));
            i4.drawImage();
            //i4.storeImage(name);
            
            ImageDisplay i5 = new ImageDisplay("PBM Index", new MaxMinClustering(name, iter, pbm, weight));
            i5.drawImage();
            //i5.storeImage(name);
        }
    }
    
    public void execute(){
        File outputFile = new File("results.csv");
        try{
            Scanner reader = new Scanner(bpiFile);
            if(!outputFile.createNewFile()){
                outputFile.delete();
                outputFile.createNewFile();
            }
            FileWriter output = new FileWriter(outputFile);
            output.write("Data Set,# Clusters,CH Index,WB Index,DB Index,RT Index,PBM Index" + "\n");
            
            weight = Double.parseDouble(JOptionPane.showInputDialog("Enter the weighting of spatial contiguity for this data set:"));
            if(weight < 0 || weight > 1){
                System.err.println("Error: Invalid weight given.");
                return;
            }
            k_MIN = Integer.parseInt(JOptionPane.showInputDialog("Enter the minimum number of clusters for this data set:"));
            k_MAX = Integer.parseInt(JOptionPane.showInputDialog("Enter the maximum number of clusters for this data set:"));        
            iter = Integer.parseInt(JOptionPane.showInputDialog("Enter the maximum number of iterations for this data set:")); 
                
            while(reader.hasNextLine()){
                name = reader.nextLine();
                MaxMinClustering m = new MaxMinClustering(name, 0, 100, weight);
                m.readData();
                clusterers = new MaxMinClustering[k_MAX - k_MIN + 1];
                for(int k = k_MIN; k <= k_MAX; k++){
                    clusterers[k-k_MIN] = new MaxMinClustering(name, iter, k, weight);
                    clusterers[k-k_MIN].execute();
                }
                output.write(name + "," + m.getTrueClusters() + "," + CHIndex() + "," + WBIndex() + "," + DBIndex() + "," + RTIndex() + "," + PBMIndex() + "\n");
            }
            output.close();
            reader.close();
            
        }catch(IOException e){
            System.err.println("Error with file reading or writing.");
        }
    }
    
    public int CHIndex(){
        //Calinski and Harabasz
        double[] clusterNumCH = new double[k_MAX-k_MIN+1];
        for(int k = k_MIN; k <= k_MAX; k++){
            clusterNumCH[k-k_MIN] = clusterers[k-k_MIN].calcSSB() * (clusterers[k-k_MIN].getNumPoints() - k) / (clusterers[k-k_MIN].calcSSE() * (k-1));
        }
        
        return k_MIN + findMinimizedK(clusterNumCH);
    }
    
    public int WBIndex(){
        //WB index of Zhao, Xu, and Franti
        double[] clusterNumWB = new double[k_MAX-k_MIN+1];
        for(int k = k_MIN; k <= k_MAX; k++){
            clusterNumWB[k-k_MIN] = k * clusterers[k-k_MIN].calcSSE() / (clusterers[k-k_MIN].calcSSB());
        }
        
        return k_MIN + findMinimizedK(clusterNumWB);
    }
    
    public int DBIndex(){
        //Davies-Bouldin Index
        double[] clusterNumDB = new double[k_MAX-k_MIN+1];
        
        for(int k = k_MIN; k <= k_MAX; k++){
            
            double temp = 0.0;
            for(int m = 0; m < k; m++){                
                temp += clusterers[k-k_MIN].calcLargestD(m);
            }

            clusterNumDB[k-k_MIN] = temp/k; 
        }
        
        return k_MIN + findMinimizedK(clusterNumDB);
    }
    
    public int RTIndex(){
        //Ray-Turi Index
        double[] clusterNumRT = new double[k_MAX-k_MIN+1];
        
        for(int k = k_MIN; k <= (k_MAX); k++){
            double intra = clusterers[k-k_MIN].calcSSE() / clusterers[k-k_MIN].getNumPoints();
            double inter = clusterers[k-k_MIN].findSmallestInterDistance();
            clusterNumRT[k-k_MIN] = intra/inter;
        }
        
        return k_MIN + findMinimizedK(clusterNumRT);
    }
    
    public int PBMIndex(){
        double[] clusterNumPBM = new double[k_MAX-k_MIN+1];
        
        for(int k = k_MIN; k <= k_MAX; k++){
            double ei = clusterers[k-k_MIN].calcEi();
            double ek = clusterers[k-k_MIN].calcEk();
            double dk = clusterers[k-k_MIN].findLargestInterDistance();
            clusterNumPBM[k-k_MIN] = ((ei * dk)/(k * ek)) * ((ei * dk)/(k * ek));
        }
        
        return k_MIN + findMaximizedK(clusterNumPBM);
    }
    
    public int findMinimizedK(double[] values){
        int minimizedIndex = 0;
        for(int x = 1; x < values.length; x++){
            if(values[x] < values[minimizedIndex]){
                minimizedIndex = x;
            }
        }
        return minimizedIndex;
    }
    
    public int findMaximizedK(double[] values){
        int maximizedIndex = 0;
        for(int x = 1; x < values.length; x++){
            if(values[x] > values[maximizedIndex]){
                maximizedIndex = x;
            }
        }
        return maximizedIndex;
    }
}
