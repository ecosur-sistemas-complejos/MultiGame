package mx.ecosur.multigame.manantiales.token
{
	import mx.ecosur.multigame.enum.Color;
	import mx.ecosur.multigame.manantiales.enum.TokenType;
	
	public class IntensiveToken extends ManantialesToken
	{				
		public function IntensiveToken()
		{
			super();
			_tooltip = "Intensive Grazing";
			_label = "I";
			_type = TokenType.INTENSIVE;
		}
		
        public override function get colorCode():uint {
            var ret:uint = _cell.colorCode;
            
            switch (_cell.color) {
                case Color.BLUE:
                    ret = 0x000082;
                    break;
                case Color.GREEN:
                    ret = 0x008200;
                    break;
                case Color.RED:
                    ret = 0x820000;
                    break;
                case Color.YELLOW:
                    ret = 0xB28F00;
                    break;
                default:
                    break;  
            }
            
            return ret;
        }	
	}
}