package mx.ecosur.multigame;

public enum Color {
	BLACK, BLUE, RED, GREEN, UNKNOWN;
	
	public Color getCompliment () {
		Color ret;
		
		switch (this) {
		case RED:
			ret = BLACK;
			break;
		case GREEN:
			ret = BLUE;
			break;
		case BLUE:
			ret = GREEN;
			break;
		case BLACK:
			ret = RED;
			break;
		default:
			ret = UNKNOWN;
		}
		
		return ret;
	}
}
