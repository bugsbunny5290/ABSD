package es.upm.platform01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class XplorationMap {
	public static Map<String, String> XplorationMap = new HashMap<String, String>();
	
	public void generateMap(){
		
		Map<String, String> contentFileMap = extractContentFile();
		
		
		int row = Integer.parseInt(contentFileMap.get("row"));
		int column = Integer.parseInt(contentFileMap.get("column"));
		
		for(int i = 1; i <= row; i++)
		{
			String[] lineMinerals = contentFileMap.get("line"+i).split(" ");
			int nmineral = 0;
			
			if(i%2 == 0){
				for (int j = 2; j <= column ; j=j+2) {
					this.XplorationMap.put(i+","+j, lineMinerals[nmineral]);
					nmineral++;
				}
			} else {
				for (int j = 1; j <= column-1 ; j=j+2) {
					this.XplorationMap.put(i+","+j, lineMinerals[nmineral]);
					nmineral++;
				}
			}
		}
	}
	
	public Map<String, String> extractContentFile()
	{
		String filePath = new File("").getAbsolutePath();
        String fileName = "\\src\\main\\java\\es\\upm\\platform01\\map";
        String line = null;
        
        Map<String, String> contentFile =  new HashMap<String, String>();
        String[] partsMap = new String[2];
        int nline = 0;
    
        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader = new FileReader(filePath + fileName);

            // Always wrap FileReader in BufferedReader.
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            while((line = bufferedReader.readLine()) != null) {
            	if (nline == 0){
            		line = line.replace("(", "");
            		line = line.replace(")", "");
            		
            		partsMap = line.split(",");
            		
            		contentFile.put("row", partsMap[0]);
            		contentFile.put("column", partsMap[1]);
            		
            		nline++;
            	} else {
            		if (partsMap.length == 2){
            			if (line != ""){
            				contentFile.put("line"+nline, line);
            				nline++;
            			}
            		}
            	}
            }   
            // Always close files.
            bufferedReader.close(); 
        }
        catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");                
        }
        catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");                  
            // Or we could just do this: 
            // ex.printStackTrace();
        }
		return contentFile;
    }

}
