package mx.ecosur.multigame.manantiales
{
	import mx.ecosur.multigame.enum.Color;
	
	public class ViveroToken extends ManantialesToken
	{
		public function ViveroToken()
		{
			super();
			_tooltip = "Vivero/Nursery";
		}
		
        public override function get colorCode():uint {
            var ret:uint = _cell.colorCode;
            
            switch (_cell.color) {
                case Color.BLUE:
                    ret = 0x8080FE;
                    break;
                case Color.GREEN:
                    ret = 0x80FE80;
                    break;
                case Color.RED:
                    ret = 0xFE8080;
                    break;
                case Color.YELLOW:
                    ret = 0xFEE680;
                    break;
                default:
                    break;  
            }
            
            return ret;
        }	
	}
}