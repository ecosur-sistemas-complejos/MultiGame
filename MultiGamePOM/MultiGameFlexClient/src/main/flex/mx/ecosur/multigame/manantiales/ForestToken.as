package mx.ecosur.multigame.manantiales
{
	import mx.controls.Image;
	import mx.ecosur.multigame.enum.Color;
	
	public class ForestToken extends ManantialesToken
	{
		public function ForestToken () {
			super ();
			_tooltip = "Managed Forest";
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