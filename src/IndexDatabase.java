
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;

/**
*
* @author François Labastie
*/
public class IndexDatabase extends JPictureNFE205{

	private String filenameEntry;
	private String filenameResult;
	JPictureNFE205 p;
	private int nbBins;
	int nbCanal;
	
	public IndexDatabase(int nbCanal, int nbBins, String filenameEntry, String filenameResult) throws JSONException{
		
		this.filenameEntry = filenameEntry;
		this.filenameResult = filenameResult;
		this.nbBins = nbBins;
		this.nbCanal = nbCanal;
		BufferedReader rd = null;
		int count = 0;

		try {
			
			JSONObject jsFile = new JSONObject();
			JSONObject jsData = new JSONObject();
			
			// Ouverture fichier en lecture.
			rd = new BufferedReader(new FileReader(new File(this.filenameEntry)));
			
			// Lecture du contenu du fichier.
			String inputLine = null;
			
			while((inputLine = rd.readLine()) != null){
								
				//System.out.println(inputLine);
				p = new JPictureNFE205("img/" + inputLine);
				
				// récupération id image
	        	String id = inputLine.substring(0, inputLine.indexOf("."));
				count++;
	        	System.out.println(id + " " + count);
				
				// traitement en niveau de gris
				if(this.nbCanal == 1){
					jsFile.put(id, p.computeGrayLevelHistogramTER(this.nbBins));
				}
				// traitement RGB
				else if(this.nbCanal == 3){
					jsFile.put(id, p.computeRGBHistogramTER(this.nbBins));
				}
								
			}
			
        	// création fichier résultat
        	FileWriter fileLevel = new FileWriter("Base10000/" + filenameResult);
        	
        	// écriture de jsFile dans fichier
        	fileLevel.write(jsFile.toString());
        	        	
        	fileLevel.close();
		}
		catch(IOException ex) {
			System.err.println("An IOException was caught!");
			ex.printStackTrace();
		}
		finally {
			// Fermeture fichier.
			try {
				rd.close();
			}
			catch (IOException ex) {
				System.err.println("An IOException was caught!");
				ex.printStackTrace();
			}
		}
	}
}
