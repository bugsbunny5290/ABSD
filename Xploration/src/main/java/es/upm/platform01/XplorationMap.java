package es.upm.platform01;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import es.upm.ontology.Location;
import es.upm.ontology.Mineral;
import es.upm.platform01.XplorationCell;

public class XplorationMap {
	
	public static Map<String, String> xplorationMap = new HashMap<String, String>();
	public static ArrayList<String> xplorationMapCellInitial = new ArrayList<String>();
	
	public static int minX=1, maxX=0;
	public static int minY=1, maxY=0;
	
	public void generateMap(){
		
		Map<String, String> contentFileMap = extractContentFile();
		
		this.maxX = Integer.parseInt(contentFileMap.get("x"));
		this.maxY  = Integer.parseInt(contentFileMap.get("y"));
		
		for(int i = 1; i <= maxX; i++)
		{
			String[] lineMinerals = contentFileMap.get("line"+i).split(" ");
			int nmineral = 0;
			
			if(i%2 == 0){
				for (int j = 2; j <= maxY ; j=j+2) {
					this.xplorationMap.put( i+","+j , lineMinerals[nmineral]);
					nmineral++;
				}
			} else {
				for (int j = 1; j <= maxY-1 ; j=j+2) {
					this.xplorationMap.put( i+","+j , lineMinerals[nmineral]);
					nmineral++;
				}
			}
		}
	}
	
	public String getMineralByCell (String varCell){
		
		String mineral = xplorationMap.get(varCell);
		return mineral;
		
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
            		
            		contentFile.put("x", partsMap[0]);
            		contentFile.put("y", partsMap[1]);
            		
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
	
	public boolean allowNextMovement (int xActual, int yActual, int xNext, int yNext){
		
		ArrayList<XplorationCell> aroundCell = new ArrayList<XplorationCell>();
		
		aroundCell = generateAroundCells (xActual, yActual); 
		
		if(aroundCell.contains(new XplorationCell(xNext, yNext))){
			return true;
		} else{
			return false;
		}
	}
	
	public boolean allowCommunication (int xInitial, int yInitial, int xReach, int yReach){
		
		ArrayList<XplorationCell> aroundCell = new ArrayList<XplorationCell>();
		
		aroundCell = generateAroundCells (xInitial, yInitial); 
		
		if(aroundCell.contains(new XplorationCell(xReach, yReach))){
			return true;
		} else{
			return false;
		}
	}
	
	public ArrayList<XplorationCell> generateAroundCells (int x, int y){
		ArrayList<XplorationCell> aroundCell = new ArrayList<XplorationCell>();
		
		
		aroundCell.add(new XplorationCell(validBorderX(x-2), validBorderY(y)));
		aroundCell.add(new XplorationCell(validBorderX(x-1), validBorderY(y+1)));
		aroundCell.add(new XplorationCell(validBorderX(x+1), validBorderY(y+1)));
		aroundCell.add(new XplorationCell(validBorderX(x+2), validBorderY(y)));
		aroundCell.add(new XplorationCell(validBorderX(x+1), validBorderY(y-1)));
		aroundCell.add(new XplorationCell(validBorderX(x-1), validBorderY(y-1)));
		
		/*
		aroundCell.add( validBorderX(x-2) + "," + validBorderY(y) );
		aroundCell.add( validBorderX(x-1) + "," + validBorderY(y+1));
		aroundCell.add( validBorderX(x+1) + "," + validBorderY(y+1));
		aroundCell.add( validBorderX(x+2) + "," + validBorderY(y) );
		aroundCell.add( validBorderX(x+1) + "," + validBorderY(y-1));
		aroundCell.add( validBorderX(x-1) + "," + validBorderY(y-1));
		*/
		return aroundCell;
	}
	
	public XplorationCell getCellDirection(int x, int y, int direction){
		ArrayList<XplorationCell> aroundCell = this.generateAroundCells (x, y);
		
		return aroundCell.get(direction);
	}
	
	public ArrayList<String> generateAroundCellsRange2 (int x, int y){
		ArrayList<String> aroundCell = new ArrayList<String>();
		
		aroundCell.add( validBorderX(x-4) + "," + validBorderY(y));
		aroundCell.add( validBorderX(x-3) + "," + validBorderY(y+1));
		aroundCell.add( validBorderX(x-2) + "," + validBorderY(y+2));
		aroundCell.add( validBorderX(x) + "," + validBorderY(y+2));
		aroundCell.add( validBorderX(x+2) + "," + validBorderY(y+2));
		aroundCell.add( validBorderX(x+3) + "," + validBorderY(y+1));
		
		aroundCell.add( validBorderX(x+4) + "," + validBorderY(y));
		aroundCell.add( validBorderX(x+3) + "," + validBorderY(y-1));
		aroundCell.add( validBorderX(x+2) + "," + validBorderY(y-2));
		aroundCell.add( validBorderX(x) + "," + validBorderY(y-2));
		aroundCell.add( validBorderX(x-2) + "," + validBorderY(y-2));
		aroundCell.add( validBorderX(x-3) + "," + validBorderY(y-1));
		
		return aroundCell;
	}
	
	
	public int validBorderX (int valueCoordinate){
		if (valueCoordinate == 0)
			valueCoordinate = this.maxX;
		else if (valueCoordinate < 0)
			valueCoordinate = this.maxX + valueCoordinate;
		else if ((valueCoordinate > maxX))
			valueCoordinate = valueCoordinate - this.maxX;
		
		return valueCoordinate;
	}
	
	public int validBorderY (int valueCoordinate){
		if (valueCoordinate == 0)
			valueCoordinate = this.maxY;
		else if (valueCoordinate < 0)
			valueCoordinate = this.maxY + valueCoordinate;
		else if ((valueCoordinate > maxY))
			valueCoordinate = valueCoordinate - this.maxY;
		
		return valueCoordinate;
	}
	
	public ArrayList<Location> getCellsForInitialRelease(int nCellsInitial) {
		
		ArrayList<Location> contentRandomCell =  new ArrayList<Location>();
		
		Random random = new Random();
		int randomX=0, randomY=0, i = 1;
	    
		while (i <= nCellsInitial ){
			randomX = showRandomInteger(this.minX, this.maxX, random);
			randomY = showRandomInteger(this.minY, this.maxY, random);
			
			Location locationObj = new Location();
			locationObj.setX(randomX);
			locationObj.setY(randomY);
			
			if (!contentRandomCell.contains(locationObj)) {
				contentRandomCell.add(locationObj);
				i++;
			}
			
	    }
		
		return contentRandomCell;
	}
	
	private static int showRandomInteger(int aStart, int aEnd, Random aRandom) {
		if (aStart > aEnd) {
			throw new IllegalArgumentException("Start cannot exceed End.");
		}
		
		//get the range, casting to long to avoid overflow problems
		long range = (long)aEnd - (long)aStart + 1;
		
		// compute a fraction of the range, 0 <= frac < range
		long fraction = (long)(range * aRandom.nextDouble());
		int randomNumber =  (int)(fraction + aStart);    

		return randomNumber;
	}

}
