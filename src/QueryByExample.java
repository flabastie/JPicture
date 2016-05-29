import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.json.JSONException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
*
* @author François Labastie
*/
public class QueryByExample extends JPictureNFE205 {
	
	private int 				nbrNeighbours;
	private int 				nbrNewCandidates;
	private String 				imageQueryName;
	private String 				imageName;
	private String 				listNamesFile;
	private String				histogramFile;
	private String[] 			selectedNeighbours;
	private ArrayList<String> 	tabNamesImages;
	private ArrayList<String> 	tabNamesVT;
	Object[][] 					namesAndDistances;
	
	
	/**
	 *
	 * QueryByExample(String imageQueryName, int nbrNeighbours, String listNamesFile, String histogramFile)
	 * 
	 */
	
	public QueryByExample(String imageQueryName, int nbrNeighbours, int nbrNewCandidates, String listNamesFile, String histogramFile) throws IOException, ParseException, JSONException{
				
		this.imageQueryName 		= imageQueryName;
		this.nbrNeighbours 			= nbrNeighbours;
		this.selectedNeighbours		= new String[nbrNeighbours];
		this.listNamesFile 			= listNamesFile;
		this.tabNamesImages 		= new ArrayList<String>();
		this.tabNamesVT 			= new ArrayList<String>();
		this.histogramFile			= histogramFile;
		this.namesAndDistances 		= new Object[nbrNeighbours][2];
		this.nbrNewCandidates		= nbrNewCandidates;
		
		String html = null;
		
		try {
			
			// Sélection de nbrNeighbours voisins placés dans selectedNeighbours
			this.selectInitialNeighbours();
			
			// loop selectedNeighbours pour calcul des distances
			for (int i = 0; i < selectedNeighbours.length; i++) {
				
				// Placement des noms images dans namesAndDistances
				String imgName = (String) this.namesAndDistances[i][0];
				
				// calcul de chaque distance
				float dist = this.calculDistance(imageQueryName, imgName);
				
				// Placement des distances dans namesAndDistances
				this.namesAndDistances[i][1] = dist;
								
				// Affichage controle
				System.out.println(imgName + " d= " + dist);								
			}
			
			Boolean bestCandidate = true;
			
			while(this.nbrNewCandidates > 0){
			
				// Recherche de distance maximum dans namesAndDistances
				int indiceMax = this.findMax();
				System.out.print(" dmax = " + this.namesAndDistances[indiceMax][0]);
				float dMax = (float) this.namesAndDistances[indiceMax][1];
				
				// Recherche d'un autre candidat
				String nameNewNeighbour = this.selectNewNeighbour();
				System.out.print(" new = " + nameNewNeighbour);
				
				// Vérification si meilleur que précédent
				float distNewNeighbour = this.calculDistance(imageQueryName, nameNewNeighbour);
				System.out.print(" dist = " + distNewNeighbour);
				
				if(distNewNeighbour < dMax){
					
					// substitution du nom image
					this.namesAndDistances[indiceMax][0] = nameNewNeighbour;
					// substitution valeur distance
					this.namesAndDistances[indiceMax][1] = distNewNeighbour;

					System.out.println(" best / changed ");
				}
				else{
					System.out.println(" BAD ");
				}
				
				this.nbrNewCandidates--;
			}
			
			// loop selectedNeighbours pour calcul des distances
			for (int i = 0; i < selectedNeighbours.length; i++) {
				
				// Placement des noms images dans namesAndDistances
				String imgName = (String) this.namesAndDistances[i][0];
				
				// calcul de chaque distance
				float dist = this.calculDistance(imageQueryName, imgName);
				
				// Placement des distances dans namesAndDistances
				this.namesAndDistances[i][1] = dist;
				
				// Affichage controle
				System.out.println(imgName + " d= " + dist);								
			}
			
			// Classement résultats par ordre croissant
			this.sortNamesAndDistances() ;
			
			// création fichier résultat
			FileWriter fileHTML = new FileWriter("result.html");
			
			html = this.createHTML(this.imageQueryName, this.namesAndDistances);
			fileHTML.write(html);
			fileHTML.close();
			
			this.readVT();
		}
		catch(IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		}
		
	}
	
	/**
	 *
	 * selectInitialNeighbours()
	 * 
	 */
	
	public void selectInitialNeighbours() throws FileNotFoundException{
				
		try {
			// Chargement liste images dans tabNamesImages
			BufferedReader br = new BufferedReader(new FileReader(this.listNamesFile));
			String line = br.readLine();
			while (line != null) {
				line = br.readLine();
			    if (line != null) {
			        line = line.substring(0, line.indexOf("."));
			        this.tabNamesImages.add(line);
			    }
			}
			// Close buffer reader
			br.close();
					
		    // Sélection random de n images
			for (int i = 0; i < this.nbrNeighbours ; i++)
		       {
		        int numRandom = (int)(Math.random()*tabNamesImages.size());
		        //this.selectedNeighbours[i] = tabNamesImages.get(numRandom);
		        
		        // mise dans namesAndDistances des noms images
		        this.namesAndDistances[i][0] = tabNamesImages.get(numRandom);
		       }
			}
		
		catch(IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		}
	}

	/**
	 *
	 * selectNewNeighbour()
	 * 
	 */
	
	public String selectNewNeighbour() throws FileNotFoundException{
				
		// nom du newNeighbour à retourner
		String newNeighbour = "";
		
		try {
			// Chargement liste images dans tabNamesImages
			BufferedReader br = new BufferedReader(new FileReader(this.listNamesFile));
			String line = br.readLine();
			while (line != null) {
				line = br.readLine();
			    if (line != null) {
			        line = line.substring(0, line.indexOf("."));
			        this.tabNamesImages.add(line);
			    }
			}
			// Close buffer reader
			br.close();
			
			// Sélection random de 1 image
			Boolean isFound = false;
			Boolean checkForPresent = true;
						
			while(checkForPresent){
				
				// sélection nombre random
				int numRandom = (int)(Math.random()*tabNamesImages.size());
				// récupération nom image correspondant
				newNeighbour = tabNamesImages.get(numRandom);
			
				// loop namesAndDistances
				for (int i = 0; i < this.nbrNeighbours ; i++){
					// Test si names équivalents
					if(this.namesAndDistances[i][0].equals(newNeighbour)){
						isFound = true;
						System.out.println("isFound");
					}
				}
				
				// nouvelle sélection nombre random
				if(isFound == true){
					numRandom = (int)(Math.random()*tabNamesImages.size());
					newNeighbour = tabNamesImages.get(numRandom);
					isFound = false;
				}
				
				// end of while
				if(isFound==false){
					checkForPresent = false;
				}	
			}			
		}		
		catch(IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		}
		return newNeighbour;
	}	
	
	/**
	 *
	 * createHTML(String imageName)
	 * 
	 */
	
	public String createHTML(String imageName, Object namesAndDistances){
				
		String html = "<!doctype html>"
				+ "<html lang='en'>"
				+ "<head>"
				+ "<meta charset='utf-8'>"
				+ "<title>HTML generated</title>"
						+ "<meta name='description' content='html images'>"
						+ "<meta name='author' content='Francois Labastie'>"
						+ "<link rel='stylesheet' href='css/styles.css'>"
						+ "<!--[if lt IE 9]>"
						+ "<script src='http://html5shiv.googlecode.com/svn/trunk/html5.js'></script>"
							+ "<![endif]-->"
					+ "</head>"
					+ "<body>"
						+ "<script src='js/scripts.js'></script>"
						+ "<img src='img/" + imageName + ".jpg'/>";
		
		for (int i = 0; i < this.nbrNeighbours ; i++){
			html = html 	+ "<img src='img/" + this.namesAndDistances[i][0] + ".jpg'/>";
		}

		html = html	+ "</body>"
				+ "</html>";
		
		return html;
	}
	
	/**
	 *
	 * readJsonFile(String imageQueryName, String histogramFile)
	 * 
	 */	
	
	public ArrayList readJsonFile(String imageQueryName, String histogramFile) throws IOException, ParseException, JSONException{
		
		JSONParser parser = new JSONParser();
		ArrayList<Float> valuesList = new ArrayList<Float>();
					
		try {

			Object obj = parser.parse(new FileReader(histogramFile));
			JSONObject jsonObject = (JSONObject) obj;			
			JSONArray msg = (JSONArray) jsonObject.get(imageQueryName);
									
			// loop array
			Iterator<Double> iterator = msg.iterator();
			while (iterator.hasNext()) {				
				String valString = String.valueOf(iterator.next());
				float valFloat = Float.parseFloat(valString);
				valuesList.add(valFloat);							
			}
				
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return valuesList;
	}
	
	/**
	 *
	 * calculDistance(String imgA, String imgB)
	 * 
	 */		
	
	public float calculDistance(String imgA, String imgB) throws FileNotFoundException, ParseException, JSONException{
		
		float distance = 0;
		ArrayList<Float> tabValuesA;
		ArrayList<Float> tabValuesB;
		
		try {
			tabValuesA = this.readJsonFile(imgA, this.histogramFile);
			tabValuesB = this.readJsonFile(imgB, this.histogramFile);
			
			for (int i = 0; i < tabValuesA.size(); i++) {
				
				// Calcul différences valeurs
				float valueDiff = (float)tabValuesA.get(i) - (float)tabValuesB.get(i);
				
				// Calcul carré
				float valueSquare = (float) Math.pow((float)valueDiff, 2.0);
				
				// Ajout à distance
				distance = distance + valueSquare;			
			}
			
			// Racine carrée
			distance = (float) Math.sqrt(distance);
			
		}
		
		catch(IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		}
		return distance;
	}

	
	/**
	 *
	 * findMax()
	 * 
	 */	
	
	public int findMax() {
		
		int indiceMax = -1;
		float max = 0;
		
		// Loop namesAndDistances
		for (int i = 0; i <nbrNeighbours; i++) {
			
			// Test distance > max
			if ( (float) this.namesAndDistances[i][1] > max){
				
				// Actualisation du max
				max = (float) this.namesAndDistances[i][1];
				
				// Sélection indice correspondant
				indiceMax = i;
				
			}
		}
		return indiceMax;
	}
	
	/**
	 *
	 * sortNamesAndDistances()
	 * 
	 */	
	
	public void sortNamesAndDistances() {
		
		float temp;
		boolean sorted = false;
		
		while(sorted == false){
			
			sorted = true;
			float f1;
			float f2;

			// Loop namesAndDistances
			for (int i = 0; i <this.namesAndDistances.length-1; i++) {
							    
			   f1 = (float)this.namesAndDistances[i][1];
			   f2 = (float)this.namesAndDistances[i+1][1];
				
			   if(f1 > f2 ) {
					temp = ((float) this.namesAndDistances[i+1][1]);
					this.namesAndDistances[i+1][1] = this.namesAndDistances[i][1];
					this.namesAndDistances[i][1] = temp;
					sorted = false;
			   }
	
			}
		}
		
		System.err.println("SORTED LIST");
		for (int i = 0; i <this.namesAndDistances.length-1; i++) {
			System.err.println(i + " " + this.namesAndDistances[i][0] + " " + this.namesAndDistances[i][1]);
		}
	}
	

	/**
	 *
	 * readVT()
	 * 
	 */
	
	public void readVT() throws FileNotFoundException{
				
		try {
			// Chargement liste images dans tabNamesImages
			BufferedReader br = new BufferedReader(new FileReader("VT_files.txt"));
			String line = br.readLine();
			
			while (line != null) {
				line = br.readLine();
			    if (line != null) {
			        //line = line.substring(0, line.indexOf("."));
			        //System.out.println("VT =  " + i + " " + line);
			        this.tabNamesVT.add(line);
			    }
			}
			// Close buffer reader
			br.close();
						
			Object[][] tabVT = new Object[this.tabNamesVT.size()][2];
			
				for (int i = 0; i < this.tabNamesVT.size() ; i++)
				{					
					String lineList = tabNamesVT.get(i);
					String[] imgLine = lineList.split(" ");
										
					for (int j = 0; j < imgLine.length ; j++){
						String nomImage = imgLine[j];
						nomImage = nomImage.substring(0, nomImage.indexOf("."));
						imgLine[j] = nomImage;
					}

					tabVT[i][0] = i;
					tabVT[i][1] = imgLine;
					//System.out.println(tabVT[i][0]);
				}	
				
				Object cls = tabVT[0][1];
				//System.out.println(((ArrayList) cls).get(0));
											
			}
		
		catch(IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		}
	}	
}
