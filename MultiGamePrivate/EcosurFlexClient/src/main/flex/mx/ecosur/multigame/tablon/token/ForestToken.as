package mx.ecosur.multigame.tablon.token
{
	import mx.controls.Image;
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.manantiales.enum.TokenType;
	
	public class ForestToken extends TablonToken
	{
		public function ForestToken () {
			super ();
			_tooltip = "Managed Forest";
			_label = "F";
			_type = TokenType.FOREST;
		}
		
        public override function get colorCode():uint {
        	var ret:uint = _cell.colorCode;
        	
        	switch (_cell.color) {
                case Color.BLUE:
                    ret = 0x0000BB;
                    break;
                case Color.GREEN:
                    ret = 0x00BB00;
                    break;
                case Color.RED:
                    ret = 0xBB0000;
                    break;
                case Color.YELLOW:
                    ret = 0xFFCC00;
                    break;
                default:
                    break;	
        	}
        	
        	return ret;
        }		
	}
}