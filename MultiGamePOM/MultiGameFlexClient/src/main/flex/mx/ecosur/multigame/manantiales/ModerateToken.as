package mx.ecosur.multigame.manantiales
{
	import mx.ecosur.multigame.enum.Color;
	   import mx.ecosur.multigame.manantiales.enum.TokenType;
	
	public class ModerateToken extends ManantialesToken
	{
		public function ModerateToken()
		{
			super();
			_tooltip = "Moderate Grazing";
			_label = "M";
			_type = TokenType.MODERATE;
		}
		
        public override function get colorCode():uint {
            var ret:uint = _cell.colorCode;
           
            switch (_cell.color) {
                case Color.BLUE:
                    ret = 0xBFBFFE;
                    break;
                case Color.GREEN:
                    ret = 0xBFFEBF;
                    break;
                case Color.RED:
                    ret = 0xFEBFBF;
                    break;
                case Color.YELLOW:
                    ret = 0xFEF2BF;
                    break;
                default:
                    break;  
            }
            
            return ret;
        }
	}
}