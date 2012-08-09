package mx.ecosur.multigame.manantiales.token
{
import mx.ecosur.multigame.manantiales.token.ManantialesToken;
import mx.ecosur.multigame.manantiales.enum.TokenType;
	import mx.ecosur.multigame.enum.Color;
	
    [ResourceBundle("ManantialesCommon")]
	public class ViveroToken extends ManantialesToken
	{
		public function ViveroToken()
		{
			super();
			_tooltip = resourceManager.getString("Manantiales", "manantiales.token.vivero");
			_label = "E";
			_type = TokenType.VIVERO;
		}
	}
}
