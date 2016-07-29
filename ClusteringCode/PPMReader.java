import java.io.*;
import java.awt.Dimension;
import java.util.ArrayList;
public class PPMReader
{
    File file;
    Point[] data;
    int width, height;
    
    PPMReader(File name){
        file = name;
    }
    
    public void readWeightedData(double weight) throws IOException, UnsupportedEncodingException{
        FileInputStream fis = new FileInputStream(file);
		Dimension dim = new Dimension();
		// XXX: The source data to the StreamTokenizer can't buffered, since we
		// only need a few lines of the beginning of the file, and all remain
		// data must be processed later. Even a InputStreamReader does some
		// internal buffering, so we must use a deprecated constructor, create
		// our own Reader or create some methods to simulate a StreamTokenizer.
		// Easy way chosen
		StreamTokenizer st = new StreamTokenizer(fis);
		st.commentChar('#');

		/* PPM file format:
		 * 
		 * #			  --> Comments allowed anywere before binary data
		 * P3|P6          --> ASCII/Binary
		 * WIDTH          --> image width, in ascii
		 * HEIGHT         --> image height, in ascii
		 * COLORS		  --> num colors, in ascii
		 * [data]		  --> if P6, data in binary, 3 RGB bytes per pixel 
		 */

		st.nextToken();
		if( !st.sval.equals("P6") )
			throw new UnsupportedEncodingException("Not a P6 (binary) PPM");
		
		st.nextToken();
		dim.width = (int) Math.round(st.nval);
		width = dim.width;
		st.nextToken();
		dim.height = (int) Math.round(st.nval);
		height = dim.height;
		data = new Point[dim.width * dim.height];

		st.nextToken(); 
		int maxVal = (int) Math.round(st.nval); 
		if( maxVal != 255 )
			throw new UnsupportedEncodingException("Not a 255 color PPM");
				
		// Binary data cann be buffered
		InputStream in = new BufferedInputStream(fis);
		int numPixels = dim.width * dim.height;
		double x = 0;
		double y = 0;
		double w = Math.sqrt(weight);
		double w2 = Math.sqrt(1-weight);
		for (int i = 0; i < numPixels; i++)
		{
		    //RGB values and x y coordinates must be scaled so that all are within the range [0,1]
		    //this makes it so no single feature carries more weight naturally when clustering
			double r = in.read() / 255.0;
			double g = in.read() / 255.0;
			double b = in.read() / 255.0;
			if( r== -1 || g == -1 || b == -1)
				throw new IOException("EOF:" + r + " " + g + " " + b);
			double[] temp = {(x*w)/(width-1), (y*w)/(height-1), r*(w2), g*(w2), b*(w2)};
			data[i] = new Point(temp);
			x++;
			if(x >= width){
			    x = 0;
			    y++;
			}
		}
		in.close();		

    }
    
    public void readUnweightedData() throws IOException, UnsupportedEncodingException{
        FileInputStream fis = new FileInputStream(file);
		Dimension dim = new Dimension();
		// XXX: The source data to the StreamTokenizer can't buffered, since we
		// only need a few lines of the beginning of the file, and all remain
		// data must be processed later. Even a InputStreamReader does some
		// internal buffering, so we must use a deprecated constructor, create
		// our own Reader or create some methods to simulate a StreamTokenizer.
		// Easy way chosen :-)
		StreamTokenizer st = new StreamTokenizer(fis);
		st.commentChar('#');

		/* PPM file format:
		 * 
		 * #			  --> Comments allowed anywere before binary data
		 * P3|P6          --> ASCII/Binary
		 * WIDTH          --> image width, in ascii
		 * HEIGHT         --> image height, in ascii
		 * COLORS		  --> num colors, in ascii
		 * [data]		  --> if P6, data in binary, 3 RGB bytes per pixel 
		 */

		st.nextToken();
		if( !st.sval.equals("P6") )
			throw new UnsupportedEncodingException("Not a P6 (binary) PPM");
		
		st.nextToken();
		dim.width = (int) Math.round(st.nval);
		width = dim.width;
		st.nextToken();
		dim.height = (int) Math.round(st.nval);
		height = dim.height;
		data = new Point[dim.width * dim.height];

		st.nextToken(); 
		int maxVal = (int) Math.round(st.nval); 
		if( maxVal != 255 )
			throw new UnsupportedEncodingException("Not a 255 color PPM");
				
		// Binary data cann be buffered
		InputStream in = new BufferedInputStream(fis);
		int numPixels = dim.width * dim.height;
		for (int i = 0; i < numPixels; i++)
		{
			int r = in.read();
			int g = in.read();
			int b = in.read();
			if( r== -1 || g == -1 || b == -1)
				throw new IOException("EOF:" + r + " " + g + " " + b);
			double[] temp = {r, g, b};
			data[i] = new Point(temp);
		}
		in.close();		

    }
    
    public int getWidth(){
        return width;
    }
    
    public int getHeight(){
        return height;
    }
    
    public Point[] getData(){
        return data;
    }
}
