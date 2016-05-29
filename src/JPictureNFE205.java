import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;


/**
 *
 * @author Marin Ferecatu
 * modified by Francois Labastie 15/04/16
 * 
 */
public class JPictureNFE205 {
    static final int RGB_DEPTH = 3, depth = 3, RED = 0, GREEN = 1, BLUE = 2;
    String filename;
    boolean readError;
    int width, height, type;
    float[][][] pixels;

    public JPictureNFE205() {
    }

    public JPictureNFE205(String fileName) {
        readImageFile(fileName);
    }

    public JPictureNFE205(int width, int height, int type, String filename, float[][][] pixels) {
        // deep constructor
        this.width = width;
        this.height = height;
        this.type = type;
        this.filename = filename;
        float[][][] pc = clonePixels(pixels);
        this.pixels = pc;
    }

    public JPictureNFE205(int width, int height) {
        this.width = width;
        this.height = height;
        type = 5; // BufferedImage.TYPE_3BYTE_BGR
        //pixels = new float[width][height][RGB_DEPTH];
        pixels = new float[width][height][RGB_DEPTH];
    }

    double max3(double a, double b, double c) {
        return ((a > b) ? ((a > c) ? a : c) : ((b > c) ? b : c));
    }

    double min3(double a, double b, double c) {
        return ((a < b) ? ((a < c) ? a : c) : ((b < c) ? b : c));
    }

    float[][][] clonePixels(float[][][] pixels) {
        int x, y;
        float[][][] pc = new float[width][height][RGB_DEPTH]; // pixels copy
        for(x = 0; x < width; x++) for(y = 0; y < height; y++) {
            System.arraycopy(pixels[x][y], 0, pc[x][y], 0, RGB_DEPTH);
        }
        return pc;
    }

    void setPixels(double[][] v) {
        width = v.length;
        height = v[0].length;
        pixels = new float[width][height][RGB_DEPTH];
        for(int x = 0; x < width; x++) for(int y = 0; y < height; y++) {
            pixels[x][y][RED] = pixels[x][y][GREEN] = pixels[x][y][BLUE] = (float)v[x][y];
        }
    }

    void readImageFile(String fileName){
        try {
            BufferedImage img = ImageIO.read(new File(fileName));
            this.filename = fileName;
            width = img.getWidth();
            height = img.getHeight();
            // type = img.getType();
            type = 5; // BufferedImage.TYPE_3BYTE_BGR
            pixels = new float[width][height][RGB_DEPTH];
            int[] rgbArray = img.getRGB(0, 0, width, height, null, 0, width);
            int pixel;
            for(int x = 0; x < width; ++x)
                for(int y = 0; y < height; ++y) {
                    pixel = rgbArray[y*width + x];
                    // int alpha = (pixel >> 24) & 0xff; // we do not use alpha
                    pixels[x][y][RED]   = (float)((pixel >> 16) & 0xff)/255.0f;
                    if(pixels[x][y][RED] == 1) pixels[x][y][RED] -= 1e-5f;
                    pixels[x][y][GREEN] = (float)((pixel >>  8) & 0xff)/255.0f;
                    if(pixels[x][y][GREEN] == 1) pixels[x][y][GREEN] -= 1e-5f;
                    pixels[x][y][BLUE]  = (float)((pixel      ) & 0xff)/255.0f;
                    if(pixels[x][y][BLUE] == 1) pixels[x][y][BLUE] -= 1e-5f;
                }
        } catch (Exception e) {
            e.printStackTrace();
            readError = true;
        }
    }

    void writeImageFile(String filename) {
        int[] rgbArray = new int[width*height];
        this.filename = filename;
        for(int x = 0; x < width; ++x)
            for(int y = 0; y < height; ++y) {
                rgbArray[y*width + x] = (((int)(pixels[x][y][RED]*255.0f) & 0xff)   << 16) |
                                        (((int)(pixels[x][y][GREEN]*255.0f) & 0xff) <<  8) |
                                        (((int)(pixels[x][y][BLUE]*255.0f) & 0xff)       );
            }
        BufferedImage img = new BufferedImage(width, height, type);
        img.setRGB(0, 0, width, height, rgbArray, 0, width);
        try {
            ImageIO.write(img, "jpg", new File(filename));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    void computeGrayLevelHistogram(int nbBins){  
    		
        try {      
        	
        	// création du tableau conteneur des nombres de pixels selon valeur
        	int[] bins = new int[nbBins];
        	
        	// nom du fichier à créer
        	String nomFichierImage = this.filename;
        	String nomFichierTexte = nomFichierImage.substring(0 , nomFichierImage.indexOf("."));
        	String id = nomFichierTexte.substring(nomFichierTexte.indexOf("/")+1);
        	System.out.println(id);

        	// création fichier
        	FileWriter fileLevel = new FileWriter(nomFichierTexte + "_GREY.json");

        	for(int i = 0; i < this.width; ++i) {
        		for(int j = 0; j < this.height; ++j) {
        			
        			// calcul de niveau de gris du pixel
        			double greyLevel = 0.299 * this.pixels[i][j][RED] + 0.587 *this.pixels[i][j][GREEN] + 0.114 * this.pixels[i][j][BLUE];
        			
        			// normalisation de greyLevel entre 0 et nbBins
        			double gNorm = greyLevel * nbBins;
        			
        			// transformation gNorm (double) en gInt (int)
        			int gInt = (int)gNorm;
        			//System.out.println(gInt);
        			
        			// incrementation du tableau bins pour chaque index == gInt
        			bins[gInt]++;
        	    }
        	}
        	
        	float widthPerHeight = width*height;
        	float countedPixels = 0;
        	float totalPercent = 0;
        	float vPercent = 0;
        	JSONObject jsFile = new JSONObject();
        	jsFile.put("id", id);
        	
        	for(int i=0; i < bins.length; ++i){
        		
        		// calculs des pourcentages
        		vPercent = bins[i]*100/widthPerHeight;
        		
        		// ajout valeurs couleurs dans object jsFile
        		jsFile.put(String.valueOf(i), vPercent);

        		// écriture des poucentages dans fichier texte
        		//fileLevel.write(vPercent + "\n");
        		
        		// total des pourcentage (vérification)
        		totalPercent += vPercent;
        		
        		// somme des pixels comptés (vérification)
        		countedPixels += bins[i];
        	}
        	
        	fileLevel.write(jsFile.toString());
        	fileLevel.close();
        	
        	// affichage des valeurs de controle en console
        	System.out.println("widthPerHeight = " + widthPerHeight);
        	System.out.println("countedPixels  = " + countedPixels);
        	System.out.println("totalPercent   = " + totalPercent);
        	  	
        } catch (Exception e) {
            e.printStackTrace();
            readError = true;
        }
    }
    
    void computeRGBlHistogram(int nbBins){  
		
        try {      
        	
        	// création des tableaux conteneurs des nombres de pixels par couleur
        	int[] redBins 	= new int[nbBins];
            int[] greenBins = new int[nbBins];
            int[] blueBins 	= new int[nbBins];
        	
        	// nom du fichier à créer
        	String nomFichierImage = this.filename;
        	String nomFichierTexte = nomFichierImage.substring(0 , nomFichierImage.indexOf("."));
        	String id = nomFichierTexte.substring(nomFichierTexte.indexOf("/")+1);
        	System.out.println(id);

        	// création du fichier
        	FileWriter fileLevel = new FileWriter(nomFichierTexte + "_RGB.json");

        	for(int i = 0; i < this.width; ++i) {
        		for(int j = 0; j < this.height; ++j) {
        			        			
        			// récupération couleurs
        			double red 		= this.pixels[i][j][RED];
        			double green 	= this.pixels[i][j][GREEN];
        			double blue 	= this.pixels[i][j][BLUE];
        			
        			// normalisation couleurs entre 0 et nbBins
        			double redNorm 		= red * nbBins;
        			double greenNorm 	= green * nbBins;
        			double blueNorm 	= blue * nbBins;
        			
        			// transformation couleur (double) en couleur (int)
        			int redInt 		= (int)redNorm;
        			int greenInt 	= (int)greenNorm;
        			int blueInt 	= (int)blueNorm;
        			
        			// incrementation des tableau bins couleurs pour chaque index == gInt
        			redBins[redInt]++;
        			greenBins[greenInt]++;
        			blueBins[blueInt]++;
        	    }
        	}
        	
        	float widthPerHeight 	= width*height;
        	float countedPixels 	= 0;
        	float totalPercent 		= 0;
        	float rPercent 			= 0;
        	float gPercent 			= 0;
        	float bPercent 			= 0;
        	JSONObject jsFile 		= new JSONObject();
        	jsFile.put("id", id);
  	
        	for(int i=0; i < nbBins; ++i){
        		
        		// calculs des pourcentages
        		rPercent = redBins[i]*100/widthPerHeight;
        		gPercent = greenBins[i]*100/widthPerHeight;
        		bPercent = blueBins[i]*100/widthPerHeight;
        		        		
        		// valeurs couleurs dans object json
        		JSONArray colorlist = new JSONArray();
        		colorlist.put(rPercent);
        		colorlist.put(gPercent);
        		colorlist.put(bPercent);
        		
        		// ajout valeurs couleurs dans object jsPixelsColorValues
        		jsFile.put(String.valueOf(i), colorlist);
        		        		
        		// total des pourcentage (vérification)
        		totalPercent += rPercent;
        		
        		// somme des pixels comptés (vérification)
        		countedPixels += redBins[i];
        	}
        	        
    		fileLevel.write(jsFile.toString());
        	fileLevel.close();
        	
        	// affichage des valeurs de controle en console
        	System.out.println("widthPerHeight = " + widthPerHeight);
        	System.out.println("countedPixels  = " + countedPixels);
        	System.out.println("totalPercent   = " + totalPercent);
        	System.out.println("fileCreated    = " + nomFichierTexte + "_RGB.json");
        	  	
        } catch (Exception e) {
            e.printStackTrace();
            readError = true;
        }
    }

    public float[] computeGrayLevelHistogramTER(int nbBins){  
    	
    	// tabHisto retourné par la fonction
    	float[] tabHisto 	= new float[nbBins];
    	
        try {      
        	// création du tableau conteneur des nombres de pixels selon valeur
        	int[] bins = new int[nbBins];
        	 
        	for(int i = 0; i < this.width; ++i) {
        		for(int j = 0; j < this.height; ++j) {
        			// calcul de niveau de gris du pixel
        			double greyLevel = 0.299 * this.pixels[i][j][RED] + 0.587 *this.pixels[i][j][GREEN] + 0.114 * this.pixels[i][j][BLUE];
        			// normalisation de greyLevel entre 0 et nbBins
        			double gNorm = greyLevel * nbBins;
        			// transformation gNorm (double) en gInt (int)
        			int gInt = (int)gNorm;
        			// incrementation du tableau bins pour chaque index == gInt
        			bins[gInt]++;
        	    }
        	}
        	
        	float widthPerHeight = this.width*this.height;
        	float vPercent = 0;
        	
        	for(int i=0; i < bins.length; ++i){
        		// calculs des pourcentages
        		vPercent = bins[i]*100/widthPerHeight;
        		
        		// array
        		//list.put(vPercent);
        		tabHisto[i] = vPercent;
        	}
        	        	        	  	
        } catch (Exception e) {
            e.printStackTrace();
            readError = true;
        }
        //System.out.println(jsFile);
        return tabHisto;
    }
    
    public float[][]  computeRGBHistogramTER(int nbBins){  
		    	
    	// tabHisto retourné par la fonction
    	float[][] tabHisto = new float[nbBins][3];
    	
        try {   
        	// création des tableaux conteneurs des nombres de pixels par couleur
        	int[] redBins 	= new int[nbBins];
            int[] greenBins = new int[nbBins];
            int[] blueBins 	= new int[nbBins];
        	
        	for(int i = 0; i < this.width; ++i) {
        		for(int j = 0; j < this.height; ++j) {
        			        			
        			// récupération couleurs
        			double red 		= this.pixels[i][j][RED];
        			double green 	= this.pixels[i][j][GREEN];
        			double blue 	= this.pixels[i][j][BLUE];
        			
        			// normalisation couleurs entre 0 et nbBins
        			double redNorm 		= red * nbBins;
        			double greenNorm 	= green * nbBins;
        			double blueNorm 	= blue * nbBins;
        			
        			// transformation couleur (double) en couleur (int)
        			int redInt 		= (int)redNorm;
        			int greenInt 	= (int)greenNorm;
        			int blueInt 	= (int)blueNorm;
        			
        			// incrementation des tableau bins couleurs pour chaque index == gInt
        			redBins[redInt]++;
        			greenBins[greenInt]++;
        			blueBins[blueInt]++;
        	    }
        	}
        	
        	float widthPerHeight = this.width*this.height;
        	float rPercent = 0;
        	float gPercent = 0;
        	float bPercent = 0;
        	  	
        	for(int i=0; i < nbBins; ++i){
        		
        		// calculs des pourcentages
        		rPercent = redBins[i]*100/widthPerHeight;
        		gPercent = greenBins[i]*100/widthPerHeight;
        		bPercent = blueBins[i]*100/widthPerHeight;
        		        		        		        		
        		// ajout valeurs couleurs dans tabHisto
   				tabHisto[i][0] = rPercent;  
   				tabHisto[i][1] = gPercent; 
   				tabHisto[i][2] = bPercent; 
        	}
        	        	                	        	  	
        } catch (Exception e) {
            e.printStackTrace();
            readError = true;
        }
        //return jsFile;
        return tabHisto;
    }
        
    public static void main(String[] args) throws JSONException, IOException, ParseException {
    	
        
    	// indexation en niveau de gris
    	
        //new IndexDatabase(1, 256, 	"Base10000_files.txt", "HistGREY_256.json");
        //new IndexDatabase(1, 64, 	"Base10000_files.txt", "HistGREY_64.json");
        //new IndexDatabase(1, 16, 	"Base10000_files.txt", "HistGREY_16.json");
        
    	// indexation RGB
    	
        //new IndexDatabase(3, 216, 	"Base10000_files.txt", "HistRGB_6x6x6.json");
        //new IndexDatabase(3, 64, 	"Base10000_files.txt", "HistRGB_4x4x4.json");
        //new IndexDatabase(3, 2, 	"Base10000_files.txt", "HistRGB_2x2x2.json");
    	    	
    	// Recherche image par similarité
    	// Paramètres : nom-image / nrbe de résultats / nbre d'itérations / fichier liste images / fichier histogrammes
    	
    	new QueryByExample("137073", 12, 10, "Base10000_files.txt", "Base10000/HistGREY_16.json");

    } 
}
