package mx.ecosur.multigame;

public enum Direction {
	
	NORTH, SOUTH, EAST, WEST, NORTHEAST, SOUTHEAST, NORTHWEST, SOUTHWEST, UNKNOWN;
	
	public Vertice getVertice () {
		
		Vertice ret = null;
		
		switch (this) {
			case NORTH:
				ret = Vertice.VERTICAL;
				break;
			case SOUTH:
				ret = Vertice.VERTICAL;
				break;
			case EAST:
				ret = Vertice.HORIZONTAL;
				break;
			case WEST:
				ret = Vertice.HORIZONTAL;
				break;
			case NORTHEAST:
				ret = Vertice.FORWARD;
				break;
			case SOUTHEAST:
				ret = Vertice.REVERSE;
				break;
			case NORTHWEST:
				ret = Vertice.REVERSE;
				break;
			case SOUTHWEST:
				ret = Vertice.FORWARD;
				break;
			default:
				ret = Vertice.UNKNOWN;
				break;
		}
		
		return ret;
	}

}
