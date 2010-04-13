package mx.ecosur.multigame.manantiales.token
{
	import mx.ecosur.multigame.manantiales.enum.TokenType;
	import mx.ecosur.multigame.enum.Color;
	
	public class ViveroToken extends ManantialesToken
	{
		public function ViveroToken()
		{
			super();
			_tooltip = "Silvopastoral Establishment";
			_label = "E";
			_type = TokenType.VIVERO;
		}
	}
}