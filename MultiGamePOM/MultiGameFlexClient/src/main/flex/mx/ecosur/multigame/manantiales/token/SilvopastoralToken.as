package mx.ecosur.multigame.manantiales.token
{   	
	import mx.ecosur.multigame.component.Token;
	
    import mx.ecosur.multigame.manantiales.enum.TokenType;
	import mx.ecosur.multigame.enum.Color;

	public class SilvopastoralToken extends ManantialesToken
	{
		public function SilvopastoralToken()
		{
			super();
			_tooltip = "Silvepastoral Active";
			_label = "S";
			_type = TokenType.SILVOPASTORAL;
		}
		
        public override function get colorCode():uint {
            var ret:uint = _cell.colorCode;
            
            switch (_cell.color) {
                case Color.BLUE:
                    ret = 0x828BEC;
                    break;
                case Color.GREEN:
                    ret = 0x82EC9D;
                    break;
                case Color.RED:
                    ret = 0xEC9482;
                    break;
                case Color.YELLOW:
                    ret = 0xEBF268;
                    break;
                default:
                    break;  
            }
            
            return ret;
        }	
	}
}