package es.upm.platform01;

public class XplorationCell {
	
	private int x;
	private int y;
	
	public XplorationCell (int x, int y){
		this.x = x;
		this.y = y;
	}

	public void setX (int x){
		this.x = x;
	}
	
	public void setY (int y){
		this.y = y;
	}
	
	public int getX(){
		return this.x;
	}
	
	public int getY(){
		return this.y;
	}

}
