package mx.ecosur.multigame;

public enum Color {
	YELLOW, BLUE, RED, GREEN, UNKNOWN;
	
	public Color getCompliment () {
		Color ret;
		
		switch (this) {
		case RED:
			ret = YELLOW;
			break;
		case GREEN:
			ret = BLUE;
			break;
		case BLUE:
			ret = GREEN;
			break;
		case YELLOW:
			ret = RED;
			break;
		default:
			ret = UNKNOWN;
		}
		
		return ret;
	}
}
